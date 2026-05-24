package com.example.password_vault.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.password_vault.data.repository.AddResult
import com.example.password_vault.data.repository.VaultRepository
import com.example.password_vault.domain.model.AccountDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FormEvent {
    object Success : FormEvent()
    data class DuplicateFound(val accountId: Long) : FormEvent()
    data class Error(val msg: String) : FormEvent()
}

@HiltViewModel
class AddUpdateViewModel @Inject constructor(
    private val repo: VaultRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    // null = Add mode; non-null = Update mode
    val accountId: Long? = savedState.get<Long>("accountId")?.takeIf { it > 0 }

    val existingDetail: StateFlow<AccountDetail?> = if (accountId != null) {
        repo.observeAccountDetail(accountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    } else {
        MutableStateFlow(null)
    }

    val name = MutableStateFlow("")
    val url = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val passwordVisible = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)

    private val _events = MutableSharedFlow<FormEvent>()
    val events: SharedFlow<FormEvent> = _events

    fun prefillFromDetail(detail: AccountDetail) {
        if (name.value.isEmpty()) {
            name.value = detail.groupName
            url.value = detail.websiteUrl
            email.value = detail.username
            password.value = detail.currentVersion?.password ?: ""
        }
    }

    fun generatePassword() {
        password.value = repo.generatePassword()
    }

    fun togglePasswordVisible() {
        passwordVisible.value = !passwordVisible.value
    }

    fun submit() {
        val nameVal = name.value.trim()
        val urlVal = url.value.trim()
        val emailVal = email.value.trim()
        val passVal = password.value

        if (emailVal.isEmpty() || passVal.isEmpty()) {
            viewModelScope.launch { _events.emit(FormEvent.Error("ID and password are required.")) }
            return
        }

        val resolvedName = if (urlVal.isNotEmpty()) {
            extractSiteNameFromUrl(urlVal).ifEmpty { emailVal }
        } else {
            emailVal
        }

        viewModelScope.launch {
            isLoading.value = true
            try {
                if (accountId == null) {
                    when (val result = repo.addCredential(resolvedName, urlVal, emailVal, passVal)) {
                        is AddResult.Created -> _events.emit(FormEvent.Success)
                        is AddResult.DuplicateFound -> _events.emit(FormEvent.DuplicateFound(result.accountId))
                    }
                } else {
                    repo.updateCredential(accountId, urlVal, emailVal, passVal)
                    _events.emit(FormEvent.Success)
                }
            } finally {
                isLoading.value = false
            }
        }
    }
}

/**
 * Extracts the site name from a URL for use as a group label.
 *
 * Examples:
 *   "http://www.github.com/user"  → "github"
 *   "https://github.com"          → "github"
 *   "www.github.com"              → "github"
 *   "github.com"                  → "github"
 *   "github"                      → "github"
 */
internal fun extractSiteNameFromUrl(url: String): String {
    var s = url.trim()

    // Strip scheme (e.g. "https://", "http://", "ftp://")
    val schemeEnd = s.indexOf("://")
    if (schemeEnd != -1) s = s.substring(schemeEnd + 3)

    // Strip leading "www."
    if (s.startsWith("www.", ignoreCase = true)) s = s.substring(4)

    // Strip path, query string, and fragment
    s = s.split('/', '?', '#').first()

    // The first label before the first dot is the site name
    s = s.split('.').first()

    return s.trim()
}

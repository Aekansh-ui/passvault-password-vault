package com.example.password_vault.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.password_vault.data.repository.AddResult
import com.example.password_vault.data.repository.VaultRepository
import com.example.password_vault.domain.model.AccountDetail
import com.example.password_vault.util.UNIT_MONTHS
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

    val accountId: Long? = savedState.get<Long>("accountId")?.takeIf { it > 0 }

    val existingDetail: StateFlow<AccountDetail?> = if (accountId != null) {
        repo.observeAccountDetail(accountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    } else {
        MutableStateFlow(null)
    }

    val url = MutableStateFlow(
        if (accountId == null) savedState.get<String>("prefillUrl").orEmpty() else ""
    )
    val email          = MutableStateFlow("")
    val password       = MutableStateFlow("")
    val passwordVisible = MutableStateFlow(false)
    val isLoading      = MutableStateFlow(false)

    // Reminder state
    val reminderEnabled = MutableStateFlow(false)
    val reminderUnit    = MutableStateFlow(UNIT_MONTHS)
    val reminderValue   = MutableStateFlow(1)

    private var prefilled = false

    private val _events = MutableSharedFlow<FormEvent>()
    val events: SharedFlow<FormEvent> = _events

    fun prefillFromDetail(detail: AccountDetail) {
        if (prefilled) return
        prefilled = true
        url.value     = detail.websiteUrl
        email.value   = detail.username
        password.value = detail.currentVersion?.password ?: ""
        reminderEnabled.value = detail.reminderEnabled
        reminderUnit.value    = detail.reminderUnit ?: UNIT_MONTHS
        reminderValue.value   = detail.reminderValue.takeIf { it > 0 } ?: 1
    }

    fun generatePassword() {
        password.value = repo.generatePassword()
    }

    fun togglePasswordVisible() {
        passwordVisible.value = !passwordVisible.value
    }

    fun submit() {
        val urlVal   = url.value.trim()
        val emailVal = email.value.trim()
        val passVal  = password.value

        if (emailVal.isEmpty() || passVal.isEmpty()) {
            viewModelScope.launch { _events.emit(FormEvent.Error("ID and password are required.")) }
            return
        }

        val resolvedName = if (urlVal.isNotEmpty()) {
            extractSiteNameFromUrl(urlVal).ifEmpty { emailVal }
        } else {
            emailVal
        }

        val remEnabled = reminderEnabled.value
        val remUnit    = if (remEnabled) reminderUnit.value else null
        val remValue   = if (remEnabled) reminderValue.value else 0

        viewModelScope.launch {
            isLoading.value = true
            try {
                if (accountId == null) {
                    when (val result = repo.addCredential(
                        resolvedName, urlVal, emailVal, passVal,
                        remEnabled, remUnit, remValue
                    )) {
                        is AddResult.Created      -> _events.emit(FormEvent.Success)
                        is AddResult.DuplicateFound -> _events.emit(FormEvent.DuplicateFound(result.accountId))
                    }
                } else {
                    repo.updateCredential(
                        accountId, urlVal, emailVal, passVal,
                        remEnabled, remUnit, remValue
                    )
                    _events.emit(FormEvent.Success)
                }
            } finally {
                isLoading.value = false
            }
        }
    }
}

internal fun extractSiteNameFromUrl(url: String): String {
    var s = url.trim()
    val schemeEnd = s.indexOf("://")
    if (schemeEnd != -1) s = s.substring(schemeEnd + 3)
    if (s.startsWith("www.", ignoreCase = true)) s = s.substring(4)
    s = s.split('/', '?', '#').first()
    s = s.split('.').first()
    return s.trim()
}

package com.example.password_vault.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.password_vault.data.repository.DeleteResult
import com.example.password_vault.data.repository.VaultRepository
import com.example.password_vault.domain.model.AccountDetail
import com.example.password_vault.domain.model.PasswordVersion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AccountEvent {
    object NavigateBack : AccountEvent()
    object GroupDeleted : AccountEvent()
    object VersionRestored : AccountEvent()
    data class NavigateToUpdate(val accountId: Long) : AccountEvent()
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repo: VaultRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    val accountId: Long = savedState["accountId"] ?: 0L

    val detail: StateFlow<AccountDetail?> = repo.observeAccountDetail(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val passwordVisible = MutableStateFlow(false)
    val selectedVersionId = MutableStateFlow<Long?>(null)

    // Derived reactive flows — safe to collectAsState() in the UI
    val displayVersion: StateFlow<PasswordVersion?> = combine(detail, selectedVersionId) { d, sid ->
        if (d == null) null
        else if (sid != null) d.versions.firstOrNull { it.id == sid }
        else d.currentVersion
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isLatest: StateFlow<Boolean> = combine(detail, selectedVersionId) { d, sid ->
        d == null || sid == null || sid == d.currentVersionId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private val _events = MutableSharedFlow<AccountEvent>()
    val events: SharedFlow<AccountEvent> = _events

    fun togglePasswordVisible() {
        passwordVisible.value = !passwordVisible.value
    }

    fun selectVersion(version: PasswordVersion) {
        selectedVersionId.value = version.id
        passwordVisible.value = false
    }

    fun selectLatestVersion() {
        selectedVersionId.value = null
        passwordVisible.value = false
    }

    fun deleteCurrentVersion() {
        viewModelScope.launch {
            when (repo.deleteCurrentVersion(accountId)) {
                DeleteResult.GroupDeleted -> _events.emit(AccountEvent.GroupDeleted)
                DeleteResult.AccountDeleted -> _events.emit(AccountEvent.NavigateBack)
                DeleteResult.PreviousVersionRestored -> {
                    selectedVersionId.value = null
                    _events.emit(AccountEvent.VersionRestored)
                }
            }
        }
    }

    fun navigateToUpdate() {
        viewModelScope.launch { _events.emit(AccountEvent.NavigateToUpdate(accountId)) }
    }
}

package com.example.password_vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.password_vault.data.preferences.AppPreferences
import com.example.password_vault.data.repository.RestoreResult
import com.example.password_vault.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsEvent {
    data class BackupReady(val json: String) : SettingsEvent()
    data class RestoreDone(val result: RestoreResult) : SettingsEvent()
    data class Error(val msg: String) : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val repo: VaultRepository
) : ViewModel() {

    val sessionTimeoutMs: StateFlow<Long> = appPreferences.sessionTimeoutMs
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppPreferences.DEFAULT_SESSION_TIMEOUT_MS
        )

    val passwordWords: StateFlow<String> = appPreferences.passwordWords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    fun setSessionTimeout(ms: Long) {
        viewModelScope.launch {
            appPreferences.setSessionTimeoutMs(ms)
        }
    }

    fun setPasswordWords(words: String) {
        viewModelScope.launch {
            appPreferences.setPasswordWords(words)
        }
    }

    fun prepareBackup() {
        viewModelScope.launch {
            try {
                val json = repo.exportToJson()
                _events.emit(SettingsEvent.BackupReady(json))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error("Backup failed: ${e.message}"))
            }
        }
    }

    fun restore(json: String) {
        viewModelScope.launch {
            try {
                val result = repo.importFromJson(json)
                _events.emit(SettingsEvent.RestoreDone(result))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error("Restore failed — file may be corrupted."))
            }
        }
    }
}

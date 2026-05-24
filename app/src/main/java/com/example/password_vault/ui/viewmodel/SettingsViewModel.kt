package com.example.password_vault.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.password_vault.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val sessionTimeoutMs: StateFlow<Long> = appPreferences.sessionTimeoutMs
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppPreferences.DEFAULT_SESSION_TIMEOUT_MS
        )

    fun setSessionTimeout(ms: Long) {
        viewModelScope.launch {
            appPreferences.setSessionTimeoutMs(ms)
        }
    }
}

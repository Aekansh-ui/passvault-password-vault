package com.example.password_vault.security

import com.example.password_vault.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val appPreferences: AppPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var authenticated = false
    private var lastActiveMs = 0L

    @Volatile
    private var timeoutMs = AppPreferences.DEFAULT_SESSION_TIMEOUT_MS

    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    init {
        scope.launch {
            appPreferences.sessionTimeoutMs.collect { ms -> timeoutMs = ms }
        }
    }

    fun onAuthenticated() {
        authenticated = true
        lastActiveMs = System.currentTimeMillis()
        _sessionExpired.value = false
    }

    fun onUserActivity() {
        if (authenticated) lastActiveMs = System.currentTimeMillis()
    }

    fun checkTimeout() {
        if (!authenticated) return
        if (System.currentTimeMillis() - lastActiveMs > timeoutMs) {
            authenticated = false
            _sessionExpired.value = true
        }
    }

    fun logout() {
        authenticated = false
        lastActiveMs = 0L
        _sessionExpired.value = true
    }

    fun isAuthenticated(): Boolean = authenticated
}

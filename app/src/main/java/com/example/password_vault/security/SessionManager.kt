package com.example.password_vault.security

import javax.inject.Inject
import javax.inject.Singleton

private const val IDLE_TIMEOUT_MS = 60_000L // 60 seconds

@Singleton
class SessionManager @Inject constructor() {
    private var authenticated = false
    private var lastActiveMs = 0L

    fun onAuthenticated() {
        authenticated = true
        lastActiveMs = System.currentTimeMillis()
    }

    fun onUserActivity() {
        if (authenticated) lastActiveMs = System.currentTimeMillis()
    }

    fun onAppForegrounded(): Boolean {
        if (!authenticated) return false
        val elapsed = System.currentTimeMillis() - lastActiveMs
        if (elapsed > IDLE_TIMEOUT_MS) {
            authenticated = false
            return false
        }
        return true
    }

    fun logout() {
        authenticated = false
        lastActiveMs = 0L
    }

    fun isAuthenticated(): Boolean = authenticated
}

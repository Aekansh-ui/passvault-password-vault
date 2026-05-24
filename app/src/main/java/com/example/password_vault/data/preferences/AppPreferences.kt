package com.example.password_vault.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sessionTimeoutKey = longPreferencesKey("session_timeout_ms")

    val sessionTimeoutMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[sessionTimeoutKey] ?: DEFAULT_SESSION_TIMEOUT_MS
    }

    suspend fun setSessionTimeoutMs(ms: Long) {
        context.dataStore.edit { prefs ->
            prefs[sessionTimeoutKey] = ms
        }
    }

    companion object {
        const val DEFAULT_SESSION_TIMEOUT_MS = 300_000L // 5 minutes
    }
}

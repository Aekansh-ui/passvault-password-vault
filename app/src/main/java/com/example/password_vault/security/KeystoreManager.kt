package com.example.password_vault.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "vault_secure_prefs"
private const val KEY_DB_PASSPHRASE = "db_passphrase"

@Singleton
class KeystoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getOrCreateDbPassphrase(): ByteArray {
        val stored = securePrefs.getString(KEY_DB_PASSPHRASE, null)
        return if (stored != null) {
            Base64.getDecoder().decode(stored)
        } else {
            val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
            securePrefs.edit()
                .putString(KEY_DB_PASSPHRASE, Base64.getEncoder().encodeToString(passphrase))
                .apply()
            passphrase
        }
    }

    fun isVaultInitialised(): Boolean =
        securePrefs.contains(KEY_DB_PASSPHRASE)
}

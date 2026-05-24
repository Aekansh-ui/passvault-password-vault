package com.example.password_vault.di

import android.content.Context
import androidx.room.Room
import com.example.password_vault.data.db.VaultDatabase
import com.example.password_vault.data.db.dao.AccountDao
import com.example.password_vault.data.db.dao.GroupDao
import com.example.password_vault.data.db.dao.PasswordVersionDao
import com.example.password_vault.data.db.dao.ProfileDao
import com.example.password_vault.security.KeystoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager
    ): VaultDatabase {
        val passphrase = keystoreManager.getOrCreateDbPassphrase()
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(context, VaultDatabase::class.java, "vault.db")
            .openHelperFactory(factory)
            .build()
    }

    @Provides fun provideGroupDao(db: VaultDatabase): GroupDao = db.groupDao()
    @Provides fun provideAccountDao(db: VaultDatabase): AccountDao = db.accountDao()
    @Provides fun provideVersionDao(db: VaultDatabase): PasswordVersionDao = db.passwordVersionDao()
    @Provides fun provideProfileDao(db: VaultDatabase): ProfileDao = db.profileDao()
}

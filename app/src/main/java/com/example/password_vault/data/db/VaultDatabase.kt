package com.example.password_vault.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.password_vault.data.db.dao.AccountDao
import com.example.password_vault.data.db.dao.GroupDao
import com.example.password_vault.data.db.dao.PasswordVersionDao
import com.example.password_vault.data.db.dao.ProfileDao
import com.example.password_vault.data.db.entity.AccountEntity
import com.example.password_vault.data.db.entity.GroupEntity
import com.example.password_vault.data.db.entity.PasswordVersionEntity
import com.example.password_vault.data.db.entity.ProfileEntity

@Database(
    entities = [
        GroupEntity::class,
        AccountEntity::class,
        PasswordVersionEntity::class,
        ProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun accountDao(): AccountDao
    abstract fun passwordVersionDao(): PasswordVersionDao
    abstract fun profileDao(): ProfileDao
}

package com.example.password_vault.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "password_versions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["account_id"]),
        Index(value = ["account_id", "created_at"])
    ]
)
data class PasswordVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "account_id") val accountId: Long,
    val password: String,
    @ColumnInfo(name = "version_no") val versionNo: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

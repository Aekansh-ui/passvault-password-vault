package com.example.password_vault.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [Index(value = ["name_normalized"], unique = true)]
)
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "name_normalized") val nameNormalized: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

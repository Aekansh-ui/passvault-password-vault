package com.example.password_vault.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "display_name") val displayName: String = "",
    val username: String = "",
    @ColumnInfo(name = "image_path") val imagePath: String? = null
)

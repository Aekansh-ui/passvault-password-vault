package com.example.password_vault.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.password_vault.data.db.entity.PasswordVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordVersionDao {

    @Query("SELECT * FROM password_versions WHERE account_id = :accountId ORDER BY version_no DESC")
    fun observeVersions(accountId: Long): Flow<List<PasswordVersionEntity>>

    @Query("SELECT * FROM password_versions WHERE account_id = :accountId ORDER BY version_no DESC")
    suspend fun getVersions(accountId: Long): List<PasswordVersionEntity>

    @Query("SELECT * FROM password_versions WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): PasswordVersionEntity?

    @Query("SELECT COUNT(*) FROM password_versions WHERE account_id = :accountId")
    suspend fun countForAccount(accountId: Long): Int

    @Query("SELECT MIN(version_no) FROM password_versions WHERE account_id = :accountId")
    suspend fun minVersionNo(accountId: Long): Int?

    @Query("DELETE FROM password_versions WHERE account_id = :accountId AND version_no = :versionNo")
    suspend fun deleteByVersionNo(accountId: Long, versionNo: Int)

    @Query("DELETE FROM password_versions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(version: PasswordVersionEntity): Long

    @Query("SELECT MAX(version_no) FROM password_versions WHERE account_id = :accountId")
    suspend fun maxVersionNo(accountId: Long): Int?
}

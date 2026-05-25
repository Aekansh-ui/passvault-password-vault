package com.example.password_vault.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.password_vault.data.db.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE group_id = :groupId ORDER BY username ASC")
    fun observeByGroup(groupId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE group_id = :groupId AND username = :username LIMIT 1")
    suspend fun findByGroupAndUsername(groupId: Long, username: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM accounts WHERE reminder_enabled = 1")
    suspend fun getAccountsWithReminder(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE reminder_enabled = 1")
    fun observeAccountsWithReminder(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY group_id ASC")
    suspend fun getAll(): List<AccountEntity>
}

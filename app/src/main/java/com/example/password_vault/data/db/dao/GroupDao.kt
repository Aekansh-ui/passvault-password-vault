package com.example.password_vault.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.password_vault.data.db.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM groups ORDER BY name_normalized ASC")
    fun observeAll(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE name_normalized LIKE '%' || :query || '%' ORDER BY name_normalized ASC")
    fun search(query: String): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE name_normalized = :normalized LIMIT 1")
    suspend fun findByNormalizedName(normalized: String): GroupEntity?

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: GroupEntity): Long

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM accounts WHERE group_id = :groupId")
    suspend fun accountCount(groupId: Long): Int
}

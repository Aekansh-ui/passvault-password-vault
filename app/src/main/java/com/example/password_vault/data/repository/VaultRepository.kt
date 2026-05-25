package com.example.password_vault.data.repository

import com.example.password_vault.domain.model.AccountDetail
import com.example.password_vault.domain.model.AccountRow
import com.example.password_vault.domain.model.GroupSummary
import com.example.password_vault.domain.model.ReminderInfo
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun observeGroups(): Flow<List<GroupSummary>>
    fun searchGroups(query: String): Flow<List<GroupSummary>>
    fun observeAccounts(groupId: Long): Flow<List<AccountRow>>
    fun observeAccountDetail(accountId: Long): Flow<AccountDetail?>

    suspend fun addCredential(
        name: String,
        url: String,
        username: String,
        password: String,
        reminderEnabled: Boolean = false,
        reminderUnit: String? = null,
        reminderValue: Int = 0
    ): AddResult

    suspend fun updateCredential(
        accountId: Long,
        url: String,
        username: String,
        newPassword: String,
        reminderEnabled: Boolean = false,
        reminderUnit: String? = null,
        reminderValue: Int = 0
    )

    suspend fun deleteCurrentVersion(accountId: Long): DeleteResult

    suspend fun getRemindableAccounts(): List<ReminderInfo>

    fun observeDueSoonGroupIds(): Flow<Set<Long>>

    fun generatePassword(): String

    suspend fun exportToJson(): String
    suspend fun importFromJson(json: String): RestoreResult
}

sealed class AddResult {
    data class Created(val accountId: Long) : AddResult()
    data class DuplicateFound(val accountId: Long) : AddResult()
}

sealed class DeleteResult {
    object PreviousVersionRestored : DeleteResult()
    object AccountDeleted : DeleteResult()
    object GroupDeleted : DeleteResult()
}

data class RestoreResult(val imported: Int, val skipped: Int)

package com.example.password_vault.data.repository

import com.example.password_vault.domain.model.AccountDetail
import com.example.password_vault.domain.model.AccountRow
import com.example.password_vault.domain.model.GroupSummary
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun observeGroups(): Flow<List<GroupSummary>>
    fun searchGroups(query: String): Flow<List<GroupSummary>>
    fun observeAccounts(groupId: Long): Flow<List<AccountRow>>
    fun observeAccountDetail(accountId: Long): Flow<AccountDetail?>

    suspend fun addCredential(name: String, url: String, username: String, password: String): AddResult
    suspend fun updateCredential(accountId: Long, url: String, username: String, newPassword: String)
    suspend fun deleteCurrentVersion(accountId: Long): DeleteResult

    fun generatePassword(): String
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

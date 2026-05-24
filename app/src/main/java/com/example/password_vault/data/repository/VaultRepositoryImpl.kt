package com.example.password_vault.data.repository

import com.example.password_vault.data.db.dao.AccountDao
import com.example.password_vault.data.db.dao.GroupDao
import com.example.password_vault.data.db.dao.PasswordVersionDao
import com.example.password_vault.data.db.entity.AccountEntity
import com.example.password_vault.data.db.entity.GroupEntity
import com.example.password_vault.data.db.entity.PasswordVersionEntity
import com.example.password_vault.domain.model.AccountDetail
import com.example.password_vault.domain.model.AccountRow
import com.example.password_vault.domain.model.GroupSummary
import com.example.password_vault.domain.model.PasswordVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.inject.Inject

private const val MAX_VERSIONS = 10

class VaultRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val accountDao: AccountDao,
    private val versionDao: PasswordVersionDao
) : VaultRepository {

    override fun observeGroups(): Flow<List<GroupSummary>> =
        groupDao.observeAll().map { list -> list.map { GroupSummary(it.id, it.name) } }

    override fun searchGroups(query: String): Flow<List<GroupSummary>> =
        groupDao.search(query.trim().lowercase()).map { list -> list.map { GroupSummary(it.id, it.name) } }

    override fun observeAccounts(groupId: Long): Flow<List<AccountRow>> =
        accountDao.observeByGroup(groupId).map { list ->
            list.map { AccountRow(it.id, it.username, it.websiteUrl, it.currentVersionId) }
        }

    override fun observeAccountDetail(accountId: Long): Flow<AccountDetail?> =
        versionDao.observeVersions(accountId).map { versions ->
            val account = accountDao.findById(accountId) ?: return@map null
            val group = groupDao.findById(account.groupId) ?: return@map null
            AccountDetail(
                accountId = accountId,
                groupId = account.groupId,
                groupName = group.name,
                username = account.username,
                websiteUrl = account.websiteUrl,
                versions = versions.map { PasswordVersion(it.id, it.versionNo, it.password, it.createdAt) },
                currentVersionId = account.currentVersionId
            )
        }

    override suspend fun addCredential(
        name: String,
        url: String,
        username: String,
        password: String
    ): AddResult {
        val normalized = name.trim().lowercase()
        val existingGroup = groupDao.findByNormalizedName(normalized)
        val groupId = if (existingGroup != null) {
            existingGroup.id
        } else {
            groupDao.insert(GroupEntity(name = name.trim(), nameNormalized = normalized))
        }

        val existing = accountDao.findByGroupAndUsername(groupId, username.trim())
        if (existing != null) return AddResult.DuplicateFound(existing.id)

        val accountId = accountDao.insert(
            AccountEntity(groupId = groupId, username = username.trim(), websiteUrl = url.trim())
        )
        val versionId = versionDao.insert(
            PasswordVersionEntity(accountId = accountId, password = password, versionNo = 1)
        )
        accountId.let { id ->
            val account = accountDao.findById(id) ?: return AddResult.Created(id)
            accountDao.update(account.copy(currentVersionId = versionId))
        }
        return AddResult.Created(accountId)
    }

    override suspend fun updateCredential(
        accountId: Long,
        url: String,
        username: String,
        newPassword: String
    ) {
        val account = accountDao.findById(accountId) ?: return
        val maxNo = versionDao.maxVersionNo(accountId) ?: 0
        val versionId = versionDao.insert(
            PasswordVersionEntity(accountId = accountId, password = newPassword, versionNo = maxNo + 1)
        )
        val count = versionDao.countForAccount(accountId)
        if (count > MAX_VERSIONS) {
            val minNo = versionDao.minVersionNo(accountId)
            if (minNo != null) versionDao.deleteByVersionNo(accountId, minNo)
        }
        accountDao.update(
            account.copy(
                websiteUrl = url.trim(),
                username = username.trim(),
                currentVersionId = versionId,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteCurrentVersion(accountId: Long): DeleteResult {
        val account = accountDao.findById(accountId) ?: return DeleteResult.AccountDeleted
        val versions = versionDao.getVersions(accountId)
        val current = versions.firstOrNull { it.id == account.currentVersionId }
            ?: versions.firstOrNull()
            ?: return DeleteResult.AccountDeleted

        versionDao.deleteById(current.id)
        val remaining = versionDao.getVersions(accountId)

        return if (remaining.isEmpty()) {
            val groupId = account.groupId
            accountDao.deleteById(accountId)
            if (groupDao.accountCount(groupId) == 0) {
                groupDao.deleteById(groupId)
                DeleteResult.GroupDeleted
            } else {
                DeleteResult.AccountDeleted
            }
        } else {
            accountDao.update(account.copy(currentVersionId = remaining.first().id))
            DeleteResult.PreviousVersionRestored
        }
    }

    override fun generatePassword(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#\$%^&*_-+="
        val rng = SecureRandom()
        return (1..16).map { chars[rng.nextInt(chars.length)] }.joinToString("")
    }
}

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
import com.example.password_vault.domain.model.ReminderInfo
import com.example.password_vault.util.isDueSoon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
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
            list.map {
                AccountRow(
                    id = it.id,
                    username = it.username,
                    websiteUrl = it.websiteUrl,
                    currentVersionId = it.currentVersionId,
                    reminderEnabled = it.reminderEnabled,
                    reminderUnit = it.reminderUnit,
                    reminderValue = it.reminderValue,
                    lastChangedAt = it.updatedAt
                )
            }
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
                currentVersionId = account.currentVersionId,
                reminderEnabled = account.reminderEnabled,
                reminderUnit = account.reminderUnit,
                reminderValue = account.reminderValue
            )
        }

    override suspend fun addCredential(
        name: String,
        url: String,
        username: String,
        password: String,
        reminderEnabled: Boolean,
        reminderUnit: String?,
        reminderValue: Int
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

        val now = System.currentTimeMillis()
        val accountId = accountDao.insert(
            AccountEntity(
                groupId = groupId,
                username = username.trim(),
                websiteUrl = url.trim(),
                createdAt = now,
                updatedAt = now,
                reminderEnabled = reminderEnabled,
                reminderUnit = if (reminderEnabled) reminderUnit else null,
                reminderValue = if (reminderEnabled) reminderValue else 0
            )
        )
        val versionId = versionDao.insert(
            PasswordVersionEntity(accountId = accountId, password = password, versionNo = 1)
        )
        accountDao.findById(accountId)?.let { account ->
            accountDao.update(account.copy(currentVersionId = versionId))
        }
        return AddResult.Created(accountId)
    }

    override suspend fun updateCredential(
        accountId: Long,
        url: String,
        username: String,
        newPassword: String,
        reminderEnabled: Boolean,
        reminderUnit: String?,
        reminderValue: Int
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
                updatedAt = System.currentTimeMillis(),
                reminderEnabled = reminderEnabled,
                reminderUnit = if (reminderEnabled) reminderUnit else null,
                reminderValue = if (reminderEnabled) reminderValue else 0
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

    override suspend fun getRemindableAccounts(): List<ReminderInfo> {
        return accountDao.getAccountsWithReminder().mapNotNull { account ->
            val unit = account.reminderUnit ?: return@mapNotNull null
            val group = groupDao.findById(account.groupId) ?: return@mapNotNull null
            ReminderInfo(
                accountId = account.id,
                groupName = group.name,
                username = account.username,
                reminderUnit = unit,
                reminderValue = account.reminderValue,
                lastChangedAt = account.updatedAt
            )
        }
    }

    override fun observeDueSoonGroupIds(): Flow<Set<Long>> =
        accountDao.observeAccountsWithReminder().map { accounts ->
            accounts.filter { a ->
                a.reminderUnit != null && a.reminderValue > 0 &&
                isDueSoon(a.updatedAt, a.reminderUnit, a.reminderValue)
            }.map { it.groupId }.toSet()
        }

    override fun generatePassword(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#\$%^&*_-+="
        val rng = SecureRandom()
        return (1..16).map { chars[rng.nextInt(chars.length)] }.joinToString("")
    }

    override suspend fun exportToJson(): String {
        val groups = groupDao.getAll()
        val accounts = accountDao.getAll()
        val versions = versionDao.getAll()

        val accountsByGroup = accounts.groupBy { it.groupId }
        val versionsByAccount = versions.groupBy { it.accountId }

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val groupsArray = JSONArray()
        for (group in groups) {
            val groupObj = JSONObject()
            groupObj.put("name", group.name)

            val accountsArray = JSONArray()
            for (account in accountsByGroup[group.id].orEmpty()) {
                val accountVersions = versionsByAccount[account.id].orEmpty()
                val currentVersion = accountVersions.firstOrNull { it.id == account.currentVersionId }
                    ?: accountVersions.firstOrNull()

                val accountObj = JSONObject()
                accountObj.put("username", account.username)
                accountObj.put("websiteUrl", account.websiteUrl)
                accountObj.put("reminderEnabled", account.reminderEnabled)
                accountObj.put("reminderUnit", account.reminderUnit ?: JSONObject.NULL)
                accountObj.put("reminderValue", account.reminderValue)
                accountObj.put("createdAt", account.createdAt)
                accountObj.put("updatedAt", account.updatedAt)
                accountObj.put("currentVersionNo", currentVersion?.versionNo ?: 1)

                val versionsArray = JSONArray()
                for (v in accountVersions) {
                    val vObj = JSONObject()
                    vObj.put("versionNo", v.versionNo)
                    vObj.put("password", v.password)
                    vObj.put("createdAt", v.createdAt)
                    versionsArray.put(vObj)
                }
                accountObj.put("versions", versionsArray)
                accountsArray.put(accountObj)
            }
            groupObj.put("accounts", accountsArray)
            groupsArray.put(groupObj)
        }
        root.put("groups", groupsArray)
        return root.toString(2)
    }

    override suspend fun importFromJson(json: String): RestoreResult {
        val root = JSONObject(json)
        val groupsArray = root.getJSONArray("groups")
        var imported = 0
        var skipped = 0

        for (i in 0 until groupsArray.length()) {
            val groupObj = groupsArray.getJSONObject(i)
            val groupName = groupObj.getString("name").trim()
            val normalized = groupName.lowercase()

            val groupId = groupDao.findByNormalizedName(normalized)?.id
                ?: groupDao.insert(GroupEntity(name = groupName, nameNormalized = normalized))

            val accountsArray = groupObj.optJSONArray("accounts") ?: continue
            for (j in 0 until accountsArray.length()) {
                val aObj = accountsArray.getJSONObject(j)
                val username = aObj.getString("username").trim()

                if (accountDao.findByGroupAndUsername(groupId, username) != null) {
                    skipped++
                    continue
                }

                val reminderUnit = if (aObj.isNull("reminderUnit")) null
                                   else aObj.optString("reminderUnit").takeIf { it.isNotEmpty() }
                val now = System.currentTimeMillis()
                val accountId = accountDao.insert(
                    AccountEntity(
                        groupId = groupId,
                        username = username,
                        websiteUrl = aObj.optString("websiteUrl", ""),
                        reminderEnabled = aObj.optBoolean("reminderEnabled", false),
                        reminderUnit = reminderUnit,
                        reminderValue = aObj.optInt("reminderValue", 0),
                        createdAt = aObj.optLong("createdAt", now),
                        updatedAt = aObj.optLong("updatedAt", now)
                    )
                )

                val currentVersionNo = aObj.optInt("currentVersionNo", 1)
                val versionsArray = aObj.optJSONArray("versions") ?: continue
                var currentVersionId = 0L
                for (k in 0 until versionsArray.length()) {
                    val vObj = versionsArray.getJSONObject(k)
                    val versionNo = vObj.getInt("versionNo")
                    val vId = versionDao.insert(
                        PasswordVersionEntity(
                            accountId = accountId,
                            password = vObj.getString("password"),
                            versionNo = versionNo,
                            createdAt = vObj.optLong("createdAt", now)
                        )
                    )
                    if (versionNo == currentVersionNo) currentVersionId = vId
                }

                accountDao.findById(accountId)?.let { acc ->
                    accountDao.update(acc.copy(currentVersionId = currentVersionId))
                }
                imported++
            }
        }
        return RestoreResult(imported, skipped)
    }
}

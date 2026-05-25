package com.example.password_vault.domain.model

data class GroupSummary(
    val id: Long,
    val name: String
)

data class AccountRow(
    val id: Long,
    val username: String,
    val websiteUrl: String,
    val currentVersionId: Long,
    val reminderEnabled: Boolean,
    val reminderUnit: String?,
    val reminderValue: Int,
    val lastChangedAt: Long
)

data class PasswordVersion(
    val id: Long,
    val versionNo: Int,
    val password: String,
    val createdAt: Long
)

data class AccountDetail(
    val accountId: Long,
    val groupId: Long,
    val groupName: String,
    val username: String,
    val websiteUrl: String,
    val versions: List<PasswordVersion>,
    val currentVersionId: Long,
    val reminderEnabled: Boolean,
    val reminderUnit: String?,
    val reminderValue: Int
) {
    val currentVersion: PasswordVersion?
        get() = versions.firstOrNull { it.id == currentVersionId }
            ?: versions.firstOrNull()
}

data class ProfileData(
    val displayName: String,
    val username: String,
    val imagePath: String?
)

data class ReminderInfo(
    val accountId: Long,
    val groupName: String,
    val username: String,
    val reminderUnit: String,
    val reminderValue: Int,
    val lastChangedAt: Long
)

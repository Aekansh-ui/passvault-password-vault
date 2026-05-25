package com.example.password_vault.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.password_vault.data.repository.VaultRepository
import com.example.password_vault.util.computeNextChangeMs
import com.example.password_vault.util.daysUntilChange
import com.example.password_vault.util.isDueSoon
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repo: VaultRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val accounts = repo.getRemindableAccounts()
        accounts.forEach { info ->
            if (isDueSoon(info.lastChangedAt, info.reminderUnit, info.reminderValue)) {
                val daysLeft = daysUntilChange(info.lastChangedAt, info.reminderUnit, info.reminderValue)
                NotificationHelper.sendReminder(
                    context = applicationContext,
                    notifId = (info.accountId % Int.MAX_VALUE).toInt(),
                    groupName = info.groupName,
                    username = info.username,
                    daysLeft = daysLeft
                )
            }
        }
        return Result.success()
    }
}

package com.emm.mybest.data.reminder

import androidx.work.WorkManager
import com.emm.mybest.domain.reminder.DebtReminderScheduler
import com.emm.mybest.domain.reminder.ReminderDelays
import kotlinx.datetime.LocalTime

internal const val DEBT_REMINDER_WORK_NAME = "debt_reminder"

class DebtReminderSchedulerImpl(
    private val workManager: WorkManager,
    private val reminderDelays: ReminderDelays,
) : DebtReminderScheduler {

    override suspend fun schedule(time: LocalTime) {
        workManager.scheduleDailyReminder<DebtReminderWorker>(DEBT_REMINDER_WORK_NAME, time, reminderDelays)
    }

    override suspend fun cancel() {
        workManager.cancelUniqueWork(DEBT_REMINDER_WORK_NAME)
    }
}

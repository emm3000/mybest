package com.emm.mybest.data.reminder

import androidx.work.WorkManager
import com.emm.mybest.domain.reminder.CheckInReminderScheduler
import com.emm.mybest.domain.reminder.ReminderDelays
import kotlinx.datetime.LocalTime

internal const val CHECK_IN_REMINDER_WORK_NAME = "check_in_reminder"

class CheckInReminderSchedulerImpl(
    private val workManager: WorkManager,
    private val reminderDelays: ReminderDelays,
) : CheckInReminderScheduler {

    override suspend fun schedule(time: LocalTime) {
        workManager.scheduleDailyReminder<CheckInReminderWorker>(CHECK_IN_REMINDER_WORK_NAME, time, reminderDelays)
    }

    override suspend fun cancel() {
        workManager.cancelUniqueWork(CHECK_IN_REMINDER_WORK_NAME)
    }
}

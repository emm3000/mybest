package com.emm.mybest.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emm.mybest.domain.reminder.CheckInReminderScheduler
import com.emm.mybest.domain.reminder.ReminderDelays
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import java.util.concurrent.TimeUnit

internal const val CHECK_IN_REMINDER_WORK_NAME = "check_in_reminder"
private const val PERIOD_DAYS = 1L

class CheckInReminderSchedulerImpl(
    private val workManager: WorkManager,
    private val reminderDelays: ReminderDelays,
) : CheckInReminderScheduler {

    override suspend fun schedule(time: LocalTime) {
        if (workManager.isReminderScheduledAt(CHECK_IN_REMINDER_WORK_NAME, time, reminderDelays.timeZone)) return
        val initialDelay = reminderDelays.untilNext(DayOfWeek.entries.toSet(), time)
        val request = PeriodicWorkRequestBuilder<CheckInReminderWorker>(PERIOD_DAYS, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .addTag(reminderTimeTag(time, reminderDelays.timeZone))
            .build()
        workManager.enqueueUniquePeriodicWork(
            CHECK_IN_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
    }

    override suspend fun cancel() {
        workManager.cancelUniqueWork(CHECK_IN_REMINDER_WORK_NAME)
    }
}

package com.emm.mybest.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emm.mybest.domain.reminder.ReminderDelays
import com.emm.mybest.domain.reminder.WeightReminderScheduler
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import java.util.concurrent.TimeUnit

internal const val WEIGHT_REMINDER_WORK_NAME = "weight_reminder"
private const val PERIOD_DAYS = 7L

class WeightReminderSchedulerImpl(
    private val workManager: WorkManager,
    private val reminderDelays: ReminderDelays,
) : WeightReminderScheduler {

    override suspend fun schedule(day: DayOfWeek, time: LocalTime) {
        val tag = reminderDayTimeTag(day, time, reminderDelays.timeZone)
        if (workManager.hasUnfinishedWorkTagged(WEIGHT_REMINDER_WORK_NAME, tag)) return
        val initialDelay = reminderDelays.untilNext(setOf(day), time)
        val request = PeriodicWorkRequestBuilder<WeightReminderWorker>(PERIOD_DAYS, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WEIGHT_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
    }

    override suspend fun cancel() {
        workManager.cancelUniqueWork(WEIGHT_REMINDER_WORK_NAME)
    }
}

package com.emm.mybest.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emm.mybest.domain.reminder.PreOutingReminderScheduler
import com.emm.mybest.domain.reminder.ReminderDelays
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import java.util.concurrent.TimeUnit

internal const val PRE_OUTING_FRIDAY_WORK_NAME = "pre_outing_reminder_friday"
internal const val PRE_OUTING_SATURDAY_WORK_NAME = "pre_outing_reminder_saturday"
private const val PERIOD_DAYS = 7L

private val WORK_NAME_BY_DAY = mapOf(
    DayOfWeek.FRIDAY to PRE_OUTING_FRIDAY_WORK_NAME,
    DayOfWeek.SATURDAY to PRE_OUTING_SATURDAY_WORK_NAME,
)

class PreOutingReminderSchedulerImpl(
    private val workManager: WorkManager,
    private val reminderDelays: ReminderDelays,
) : PreOutingReminderScheduler {

    override suspend fun schedule(time: LocalTime) {
        WORK_NAME_BY_DAY.forEach { (day, workName) -> enqueue(day, workName, time) }
    }

    override suspend fun cancel() {
        WORK_NAME_BY_DAY.values.forEach { workName -> workManager.cancelUniqueWork(workName) }
    }

    private suspend fun enqueue(day: DayOfWeek, workName: String, time: LocalTime) {
        if (workManager.isReminderScheduledAt(workName, time, reminderDelays.timeZone)) return
        val initialDelay = reminderDelays.untilNext(setOf(day), time)
        val request = PeriodicWorkRequestBuilder<PreOutingReminderWorker>(PERIOD_DAYS, TimeUnit.DAYS)
            .setInitialDelay(initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .addTag(reminderTimeTag(time, reminderDelays.timeZone))
            .build()
        workManager.enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
    }
}

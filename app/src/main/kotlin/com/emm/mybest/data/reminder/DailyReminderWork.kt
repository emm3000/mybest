package com.emm.mybest.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emm.mybest.domain.reminder.ReminderDelays
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import java.util.concurrent.TimeUnit

internal const val DAILY_REMINDER_PERIOD_DAYS = 1L

/**
 * Schedules [W] as the unique work [workName] every day at [time]. Keeps unfinished work tagged with the same time
 * and time zone; otherwise cancels and re-enqueues it.
 */
internal suspend inline fun <reified W : ListenableWorker> WorkManager.scheduleDailyReminder(
    workName: String,
    time: LocalTime,
    reminderDelays: ReminderDelays,
) {
    if (isReminderScheduledAt(workName, time, reminderDelays.timeZone)) return
    val initialDelay = reminderDelays.untilNext(DayOfWeek.entries.toSet(), time)
    val request = PeriodicWorkRequestBuilder<W>(DAILY_REMINDER_PERIOD_DAYS, TimeUnit.DAYS)
        .setInitialDelay(initialDelay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .addTag(reminderTimeTag(time, reminderDelays.timeZone))
        .build()
    enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
}

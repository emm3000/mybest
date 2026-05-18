package com.emm.mybest.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emm.mybest.domain.reminder.WeightReminderScheduler
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

internal const val WEIGHT_REMINDER_WORK_NAME = "weight_reminder"
private const val HOURS_IN_DAY = 24L
private const val MS_PER_DAY = 24L * 60 * 60 * 1000

class WeightReminderSchedulerImpl(
    private val workManager: WorkManager,
    @Suppress("UnusedPrivateMember")
    private val context: Context,
) : WeightReminderScheduler {

    override suspend fun schedule(time: LocalTime) {
        val initialDelay = calculateInitialDelayMs(time)
        val request = PeriodicWorkRequestBuilder<WeightReminderWorker>(
            HOURS_IN_DAY,
            TimeUnit.HOURS,
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WEIGHT_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    override suspend fun cancel() {
        workManager.cancelUniqueWork(WEIGHT_REMINDER_WORK_NAME)
    }

    private fun calculateInitialDelayMs(targetTime: LocalTime): Long {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(tz).date
        val targetToday = LocalDateTime(today, targetTime)
        val targetTodayInstant = targetToday.toInstant(tz)
        val differenceMs = targetTodayInstant.toEpochMilliseconds() - now.toEpochMilliseconds()
        return if (differenceMs > 0L) differenceMs else differenceMs + MS_PER_DAY
    }
}

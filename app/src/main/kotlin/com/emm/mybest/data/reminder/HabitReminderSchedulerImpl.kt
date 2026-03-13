package com.emm.mybest.data.reminder

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.reminder.HabitReminderScheduler
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private const val REMINDER_HOUR_DEFAULT = 20
private const val REMINDER_MINUTE_DEFAULT = 0

class HabitReminderSchedulerImpl(
    private val workManager: WorkManager,
) : HabitReminderScheduler {

    override suspend fun schedulePreventiveReminder(habit: Habit) {
        val inputData = Data.Builder()
            .putString(HabitReminderWorker.KEY_HABIT_ID, habit.id)
            .putString(HabitReminderWorker.KEY_HABIT_NAME, habit.name)
            .putString(
                HabitReminderWorker.KEY_SCHEDULED_DAYS,
                habit.scheduledDays.joinToString(",") { it.name },
            )
            .build()

        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS)
            .setInputData(inputData)
            .setInitialDelay(calculateInitialDelayMillis(), TimeUnit.MILLISECONDS)
            .addTag(habitReminderUniqueWorkName(habit.id))
            .build()

        workManager.enqueueUniquePeriodicWork(
            habitReminderUniqueWorkName(habit.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    override suspend fun cancelReminder(habitId: String) {
        workManager.cancelUniqueWork(habitReminderUniqueWorkName(habitId))
    }

    private fun calculateInitialDelayMillis(): Long {
        val now = ZonedDateTime.now()
        var nextRun = now
            .withHour(REMINDER_HOUR_DEFAULT)
            .withMinute(REMINDER_MINUTE_DEFAULT)
            .withSecond(0)
            .withNano(0)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun).toMillis()
    }
}

internal fun habitReminderUniqueWorkName(habitId: String): String = "habit-reminder-$habitId"

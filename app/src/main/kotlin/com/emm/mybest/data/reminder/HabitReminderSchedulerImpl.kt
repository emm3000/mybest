package com.emm.mybest.data.reminder

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.reminder.HabitReminderScheduler
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class HabitReminderSchedulerImpl(
    private val workManager: WorkManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) : HabitReminderScheduler {

    override suspend fun schedulePreventiveReminder(habit: Habit) {
        if (!habit.isEnabled || !habit.reminderEnabled) {
            cancelReminder(habit.id)
            return
        }

        val (defaultHour, defaultMinute) = userPreferencesRepository.defaultReminderTime.first()
        val effectiveHour = habit.reminderHour ?: defaultHour
        val effectiveMinute = habit.reminderMinute ?: defaultMinute

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
            .setInitialDelay(calculateInitialDelayMillis(effectiveHour, effectiveMinute), TimeUnit.MILLISECONDS)
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

    private fun calculateInitialDelayMillis(hour: Int, minute: Int): Long {
        val now = ZonedDateTime.now()
        var nextRun = now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun).toMillis()
    }
}

internal fun habitReminderUniqueWorkName(habitId: String): String = "habit-reminder-$habitId"

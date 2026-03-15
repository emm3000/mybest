package com.emm.mybest.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emm.mybest.R
import com.emm.mybest.core.datetime.currentDate
import com.emm.mybest.data.AppDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.Locale

private val Context.reminderDataStore by preferencesDataStore(name = USER_PREFERENCES_DATASTORE)

class HabitReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getString(KEY_HABIT_ID)
        val habitName = inputData.getString(KEY_HABIT_NAME).orEmpty().ifBlank { "Tu hábito" }
        val scheduledDays = parseScheduledDays(inputData.getString(KEY_SCHEDULED_DAYS))
        val isReminderDay = isTodayInScheduledDays(scheduledDays)
        val isCompletedToday = if (habitId != null) {
            isHabitCompletedToday(habitId)
        } else {
            false
        }
        val shouldNotify = shouldDispatchReminder(
            habitId = habitId,
            isReminderDay = isReminderDay,
            isCompletedToday = isCompletedToday,
            canPostNotifications = canPostNotifications() && areInAppRemindersEnabled(),
        )
        if (shouldNotify) {
            val safeHabitId = habitId ?: return Result.success()
            createReminderChannelIfNeeded()

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_progress)
                .setContentTitle("Recordatorio de hábito")
                .setContentText("Hoy toca: $habitName")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notifyReminderSafely(habitId = safeHabitId, notification = notification)
        }
        return Result.success()
    }

    private fun notifyReminderSafely(
        habitId: String,
        notification: android.app.Notification,
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        runCatching {
            NotificationManagerCompat.from(applicationContext).notify(habitId.hashCode(), notification)
        }
    }

    private suspend fun isHabitCompletedToday(habitId: String): Boolean {
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME,
        ).build()

        return try {
            database.habitRecordDao()
                .getRecordForDate(habitId, currentDate())
                ?.isCompleted == true
        } finally {
            database.close()
        }
    }

    private fun isTodayInScheduledDays(scheduledDays: Set<String>): Boolean {
        if (scheduledDays.isEmpty()) return true
        val today = LocalDate.now()
            .dayOfWeek
            .name
            .uppercase(Locale.US)
        return scheduledDays.contains(today)
    }

    private fun parseScheduledDays(days: String?): Set<String> {
        if (days.isNullOrBlank()) return emptySet()
        return days
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.uppercase(Locale.US) }
            .toSet()
    }

    private fun canPostNotifications(): Boolean {
        if (!hasNotificationPermissionGranted()) return false
        return NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
    }

    private fun hasNotificationPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun areInAppRemindersEnabled(): Boolean {
        val key = booleanPreferencesKey(NOTIFICATIONS_ENABLED_KEY)
        return applicationContext.reminderDataStore.data.first()[key] ?: true
    }

    private fun createReminderChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios de hábitos",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Recordatorios preventivos para completar hábitos diarios."
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_HABIT_NAME = "habit_name"
        const val KEY_SCHEDULED_DAYS = "scheduled_days"
        private const val CHANNEL_ID = "habit_reminders"
        private const val DATABASE_NAME = "my_best_db"
        private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    }
}

private const val USER_PREFERENCES_DATASTORE = "user_preferences"

internal fun shouldDispatchReminder(
    habitId: String?,
    isReminderDay: Boolean,
    isCompletedToday: Boolean,
    canPostNotifications: Boolean,
): Boolean = habitId != null &&
    isReminderDay &&
    !isCompletedToday &&
    canPostNotifications

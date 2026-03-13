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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emm.mybest.R
import java.time.LocalDate
import java.util.Locale

class HabitReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getString(KEY_HABIT_ID)
        val habitName = inputData.getString(KEY_HABIT_NAME).orEmpty().ifBlank { "Tu hábito" }
        val scheduledDays = parseScheduledDays(inputData.getString(KEY_SCHEDULED_DAYS))
        val shouldNotify = habitId != null &&
            isTodayInScheduledDays(scheduledDays) &&
            canPostNotifications()
        if (shouldNotify) {
            createReminderChannelIfNeeded()

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_progress)
                .setContentTitle("Recordatorio de hábito")
                .setContentText("Hoy toca: $habitName")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(applicationContext).notify(habitId.hashCode(), notification)
        }
        return Result.success()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) return false
        }
        return NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
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
    }
}

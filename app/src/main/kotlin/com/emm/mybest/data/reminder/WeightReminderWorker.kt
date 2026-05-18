package com.emm.mybest.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emm.mybest.MainActivity
import com.emm.mybest.R

internal const val WEIGHT_REMINDER_CHANNEL_ID = "weight_reminder"
private const val WEIGHT_REMINDER_NOTIFICATION_ID = 1001

class WeightReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        ensureNotificationChannel()
        showNotification()
        return Result.success()
    }

    private fun ensureNotificationChannel() {
        val channel = NotificationChannel(
            WEIGHT_REMINDER_CHANNEL_ID,
            "Recordatorio de peso",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Recordatorio diario para registrar el peso"
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.emm.mybest.ACTION_ADD_WEIGHT"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            WEIGHT_REMINDER_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, WEIGHT_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_progress_monochrome)
            .setContentTitle("Hora de registrar tu peso")
            .setContentText("Toca para registrarlo")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(WEIGHT_REMINDER_NOTIFICATION_ID, notification)
    }
}

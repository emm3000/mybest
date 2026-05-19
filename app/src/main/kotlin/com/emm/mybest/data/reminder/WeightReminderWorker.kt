package com.emm.mybest.data.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emm.mybest.MainActivity
import com.emm.mybest.R
import com.emm.mybest.core.notification.WEIGHT_REMINDER_CHANNEL_ID
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val WEIGHT_REMINDER_NOTIFICATION_ID = 1001

class WeightReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val userPreferencesRepository: UserPreferencesRepository by inject()

    override suspend fun doWork(): Result {
        val notificationsEnabled = userPreferencesRepository.notificationsEnabled.first()
        if (!notificationsEnabled) return Result.success()

        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.emm.mybest.ACTION_ADD_WEIGHT"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
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

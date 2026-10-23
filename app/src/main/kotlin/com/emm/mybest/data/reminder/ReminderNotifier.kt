package com.emm.mybest.data.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.emm.mybest.MainActivity
import com.emm.mybest.R

class ReminderNotifier(private val context: Context) {

    fun show(notification: ReminderNotification) {
        val content = notification.content
        val built = NotificationCompat.Builder(context, notification.channelId)
            .setSmallIcon(R.drawable.ic_progress_monochrome)
            .setContentTitle(context.getString(content.titleRes))
            .setContentText(context.getString(content.textRes))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppIntent(notification))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notification.notificationId, built)
    }

    private fun openAppIntent(notification: ReminderNotification): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = notification.content.action
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            notification.notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

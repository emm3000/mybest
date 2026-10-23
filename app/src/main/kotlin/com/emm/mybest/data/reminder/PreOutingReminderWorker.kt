package com.emm.mybest.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emm.mybest.R
import com.emm.mybest.core.navigation.ACTION_PRE_OUTING
import com.emm.mybest.core.notification.PRE_OUTING_REMINDER_CHANNEL_ID
import com.emm.mybest.domain.usecase.reminder.IsPreOutingReminderDueUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val PRE_OUTING_NOTIFICATION_ID = 1002

private val PRE_OUTING_NOTIFICATION = ReminderNotification(
    channelId = PRE_OUTING_REMINDER_CHANNEL_ID,
    notificationId = PRE_OUTING_NOTIFICATION_ID,
    content = ReminderContent(
        action = ACTION_PRE_OUTING,
        titleRes = R.string.pre_outing_reminder_title,
        textRes = R.string.pre_outing_reminder_text,
    ),
)

class PreOutingReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val isPreOutingReminderDue: IsPreOutingReminderDueUseCase by inject()
    private val reminderNotifier: ReminderNotifier by inject()

    override suspend fun doWork(): Result {
        if (isPreOutingReminderDue()) {
            reminderNotifier.show(PRE_OUTING_NOTIFICATION)
        }
        return Result.success()
    }
}

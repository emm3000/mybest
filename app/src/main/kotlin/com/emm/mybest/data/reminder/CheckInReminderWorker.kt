package com.emm.mybest.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emm.mybest.R
import com.emm.mybest.core.navigation.ACTION_CHECK_IN
import com.emm.mybest.core.notification.CHECK_IN_REMINDER_CHANNEL_ID
import com.emm.mybest.domain.usecase.reminder.IsCheckInReminderDueUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val CHECK_IN_NOTIFICATION_ID = 1003

private val CHECK_IN_NOTIFICATION = ReminderNotification(
    channelId = CHECK_IN_REMINDER_CHANNEL_ID,
    notificationId = CHECK_IN_NOTIFICATION_ID,
    content = ReminderContent(
        action = ACTION_CHECK_IN,
        titleRes = R.string.check_in_reminder_title,
        textRes = R.string.check_in_reminder_text,
    ),
)

class CheckInReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val reminderNotifier: ReminderNotifier by inject()
    private val isCheckInReminderDue: IsCheckInReminderDueUseCase by inject()

    override suspend fun doWork(): Result {
        if (isCheckInReminderDue()) {
            reminderNotifier.show(CHECK_IN_NOTIFICATION)
        }
        return Result.success()
    }
}

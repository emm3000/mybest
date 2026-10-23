package com.emm.mybest.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emm.mybest.R
import com.emm.mybest.core.navigation.ACTION_ADD_WEIGHT
import com.emm.mybest.core.notification.WEIGHT_REMINDER_CHANNEL_ID
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val WEIGHT_REMINDER_NOTIFICATION_ID = 1001

private val WEIGH_IN_NOTIFICATION = ReminderNotification(
    channelId = WEIGHT_REMINDER_CHANNEL_ID,
    notificationId = WEIGHT_REMINDER_NOTIFICATION_ID,
    content = ReminderContent(
        action = ACTION_ADD_WEIGHT,
        titleRes = R.string.weight_reminder_title,
        textRes = R.string.weight_reminder_text,
    ),
)

class WeightReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val userPreferencesRepository: UserPreferencesRepository by inject()
    private val reminderNotifier: ReminderNotifier by inject()

    override suspend fun doWork(): Result {
        if (userPreferencesRepository.notificationsEnabled.first()) {
            reminderNotifier.show(WEIGH_IN_NOTIFICATION)
        }
        return Result.success()
    }
}

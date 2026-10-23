package com.emm.mybest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.annotation.StringRes
import com.emm.mybest.core.coroutines.APP_SCOPE_NAME
import com.emm.mybest.core.notification.CHECK_IN_REMINDER_CHANNEL_ID
import com.emm.mybest.core.notification.PHOTO_REMINDER_CHANNEL_ID
import com.emm.mybest.core.notification.PRE_OUTING_REMINDER_CHANNEL_ID
import com.emm.mybest.core.notification.WEIGHT_REMINDER_CHANNEL_ID
import com.emm.mybest.di.appModule
import com.emm.mybest.domain.usecase.reminder.SyncRemindersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named

private data class ChannelSpec(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
)

private val CHANNEL_SPECS = listOf(
    ChannelSpec(
        WEIGHT_REMINDER_CHANNEL_ID,
        R.string.weight_reminder_channel_name,
        R.string.weight_reminder_channel_description,
    ),
    ChannelSpec(
        PRE_OUTING_REMINDER_CHANNEL_ID,
        R.string.pre_outing_reminder_channel_name,
        R.string.pre_outing_reminder_channel_description,
    ),
    ChannelSpec(
        CHECK_IN_REMINDER_CHANNEL_ID,
        R.string.check_in_reminder_channel_name,
        R.string.check_in_reminder_channel_description,
    ),
    ChannelSpec(
        PHOTO_REMINDER_CHANNEL_ID,
        R.string.photo_reminder_channel_name,
        R.string.photo_reminder_channel_description,
    ),
)

class MyBestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyBestApp)
            modules(appModule)
        }
        ensureNotificationChannels()
        startReminderSync()
    }

    private fun ensureNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        CHANNEL_SPECS.forEach { spec -> notificationManager.createNotificationChannel(createChannel(spec)) }
    }

    private fun createChannel(spec: ChannelSpec): NotificationChannel =
        NotificationChannel(spec.id, getString(spec.nameRes), NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = getString(spec.descriptionRes)
        }

    private fun startReminderSync() {
        get<CoroutineScope>(named(APP_SCOPE_NAME)).launch { get<SyncRemindersUseCase>()() }
    }
}

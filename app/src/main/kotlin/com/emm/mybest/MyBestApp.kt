package com.emm.mybest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.annotation.StringRes
import com.emm.mybest.core.notification.WEIGHT_REMINDER_CHANNEL_ID
import com.emm.mybest.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

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
    }

    private fun ensureNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        CHANNEL_SPECS.forEach { spec -> notificationManager.createNotificationChannel(createChannel(spec)) }
    }

    private fun createChannel(spec: ChannelSpec): NotificationChannel =
        NotificationChannel(spec.id, getString(spec.nameRes), NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = getString(spec.descriptionRes)
        }
}

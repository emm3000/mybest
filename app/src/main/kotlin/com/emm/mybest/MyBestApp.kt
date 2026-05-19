package com.emm.mybest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.emm.mybest.core.notification.WEIGHT_REMINDER_CHANNEL_ID
import com.emm.mybest.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

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
        val channel = NotificationChannel(
            WEIGHT_REMINDER_CHANNEL_ID,
            "Recordatorio de peso",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Recordatorio diario para registrar el peso"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

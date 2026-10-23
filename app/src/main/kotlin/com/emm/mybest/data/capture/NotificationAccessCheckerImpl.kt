package com.emm.mybest.data.capture

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import com.emm.mybest.domain.repository.NotificationAccessChecker

class NotificationAccessCheckerImpl(
    private val context: Context,
) : NotificationAccessChecker {

    override fun isGranted(): Boolean {
        val listener = ComponentName(context, NotificationCaptureService::class.java)
        return context.getSystemService(NotificationManager::class.java)
            ?.isNotificationListenerAccessGranted(listener) == true
    }
}

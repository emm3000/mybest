package com.emm.mybest.data.reminder

import android.app.NotificationManager
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

private const val NOTIFICATION_TIMEOUT_MS = 5_000L
private const val POLL_INTERVAL_MS = 50L

internal suspend fun NotificationManager.awaitActiveNotifications(
    isReady: (List<StatusBarNotification>) -> Boolean,
): List<StatusBarNotification> = withTimeout(NOTIFICATION_TIMEOUT_MS) {
    var active = activeNotifications.toList()
    while (!isReady(active)) {
        delay(POLL_INTERVAL_MS)
        active = activeNotifications.toList()
    }
    active
}

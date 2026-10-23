package com.emm.mybest.data.capture

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.domain.models.PostedNotification
import com.emm.mybest.domain.usecase.capture.CaptureNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Instant

/**
 * Hands every posted notification to [CaptureNotificationUseCase], which keeps only those from selected apps.
 */
class NotificationCaptureService : NotificationListenerService(), KoinComponent {

    private val captureNotification: CaptureNotificationUseCase by inject()
    private val dispatchers: CoroutineDispatchers by inject()
    private val scope by lazy { CoroutineScope(SupervisorJob() + dispatchers.io) }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.toPostedNotification()
        scope.launch { captureNotification(notification) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

private fun StatusBarNotification.toPostedNotification(): PostedNotification {
    val extras = notification.extras
    return PostedNotification(
        packageName = packageName,
        title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
        text = bodyOf(
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT),
            text = extras.getCharSequence(Notification.EXTRA_TEXT),
        ),
        postedAt = Instant.fromEpochMilliseconds(postTime),
    )
}

package com.emm.mybest.data.capture

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.emm.mybest.core.coroutines.CoroutineDispatchers
import com.emm.mybest.domain.usecase.capture.CaptureNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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

package com.emm.mybest.domain.usecase.capture

import com.emm.mybest.domain.models.PostedNotification
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import kotlinx.coroutines.flow.first

class CaptureNotificationUseCase(
    private val repository: NotificationCaptureRepository,
) {

    /**
     * Stores [notification] exactly as received when its app is selected for capture and it has a title or a text (ADR 0002).
     */
    suspend operator fun invoke(notification: PostedNotification) {
        if (notification.title.isNullOrBlank() && notification.text.isNullOrBlank()) return
        if (notification.packageName !in repository.observeSelectedApps().first()) return
        repository.saveCaptured(notification.packageName, notification.title, notification.text, notification.postedAt)
    }
}

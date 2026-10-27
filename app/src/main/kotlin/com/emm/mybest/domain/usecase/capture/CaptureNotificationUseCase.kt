package com.emm.mybest.domain.usecase.capture

import com.emm.mybest.domain.models.PostedNotification
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import kotlinx.coroutines.flow.first

class CaptureNotificationUseCase(
    private val repository: NotificationCaptureRepository,
) {

    /**
     * Stores [notification] exactly as received when its app is selected for capture, it has a title or a text,
     * and it is not a group summary (ADR 0002). An update is skipped when the latest stored row with the same key
     * has the same title and text; a stored row is never modified.
     */
    suspend operator fun invoke(notification: PostedNotification) {
        if (notification.isEmpty() || notification.isGroupSummary) return
        if (notification.packageName !in repository.observeSelectedApps().first()) return
        if (repeatsLatestStored(notification)) return
        repository.saveCaptured(notification)
    }

    private suspend fun repeatsLatestStored(notification: PostedNotification): Boolean {
        val latest = repository.latestCapturedWithKey(notification.key) ?: return false
        return latest.title == notification.title && latest.text == notification.text
    }

    private fun PostedNotification.isEmpty(): Boolean = title.isNullOrBlank() && text.isNullOrBlank()
}

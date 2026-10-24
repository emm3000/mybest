package com.emm.mybest.testing

import com.emm.mybest.domain.models.CapturedNotification
import com.emm.mybest.domain.models.PostedNotification
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNotificationCaptureRepository(
    selectedApps: Set<String> = emptySet(),
) : NotificationCaptureRepository {

    private val selected = MutableStateFlow(selectedApps)
    private val captured = MutableStateFlow(emptyList<CapturedNotification>())

    override fun observeSelectedApps(): Flow<Set<String>> = selected

    override suspend fun setAppSelected(packageName: String, isSelected: Boolean) {
        selected.value = if (isSelected) selected.value + packageName else selected.value - packageName
    }

    override suspend fun clearSelectedApps() {
        selected.value = emptySet()
    }

    override fun observeCaptured(): Flow<List<CapturedNotification>> = captured

    override suspend fun saveCaptured(notification: PostedNotification) {
        val stored = CapturedNotification(
            id = captured.value.size + 1L,
            packageName = notification.packageName,
            title = notification.title,
            text = notification.text,
            postedAt = notification.postedAt,
        )
        captured.value = listOf(stored) + captured.value
    }

    override suspend fun deleteAllCaptured() {
        captured.value = emptyList()
    }
}

package com.emm.mybest.testing

import com.emm.mybest.domain.models.CapturedNotification
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant

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

    override suspend fun saveCaptured(packageName: String, title: String?, text: String?, postedAt: Instant) {
        val notification = CapturedNotification(captured.value.size + 1L, packageName, title, text, postedAt)
        captured.value = listOf(notification) + captured.value
    }

    override suspend fun deleteAllCaptured() {
        captured.value = emptyList()
    }
}

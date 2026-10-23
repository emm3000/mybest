package com.emm.mybest.domain.usecase.capture

import com.emm.mybest.domain.models.CaptureSummary
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveCaptureSummaryUseCase(
    private val repository: NotificationCaptureRepository,
) {

    /**
     * Emits how many apps are selected for capture.
     */
    operator fun invoke(): Flow<CaptureSummary> =
        repository.observeSelectedApps().map { CaptureSummary(it.size) }
}

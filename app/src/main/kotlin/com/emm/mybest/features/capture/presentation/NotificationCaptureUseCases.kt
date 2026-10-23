package com.emm.mybest.features.capture.presentation

import com.emm.mybest.domain.repository.NotificationAccessChecker
import com.emm.mybest.domain.repository.NotificationCaptureRepository
import com.emm.mybest.domain.usecase.capture.ObserveCaptureOverviewUseCase

data class NotificationCaptureUseCases(
    val observeOverview: ObserveCaptureOverviewUseCase,
    val repository: NotificationCaptureRepository,
    val accessChecker: NotificationAccessChecker,
)

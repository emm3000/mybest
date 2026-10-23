package com.emm.mybest.features.capture.presentation

data class NotificationCaptureState(
    val isLoaded: Boolean = false,
    val isAccessGranted: Boolean = false,
    val isCaptureActive: Boolean = false,
    val apps: List<CaptureAppItemState> = emptyList(),
    val notifications: List<CapturedItemState> = emptyList(),
    val isDeleteConfirmationVisible: Boolean = false,
)

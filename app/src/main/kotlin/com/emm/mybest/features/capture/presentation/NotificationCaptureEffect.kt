package com.emm.mybest.features.capture.presentation

sealed interface NotificationCaptureEffect {
    data object ActionFailed : NotificationCaptureEffect
    data object OpenAccessSettings : NotificationCaptureEffect
}

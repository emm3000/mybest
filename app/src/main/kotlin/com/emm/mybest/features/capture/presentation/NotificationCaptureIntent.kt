package com.emm.mybest.features.capture.presentation

sealed interface NotificationCaptureIntent {
    data object OnResume : NotificationCaptureIntent
    data object OnOpenAccessSettingsClick : NotificationCaptureIntent
    data class OnAppToggle(val packageName: String, val isSelected: Boolean) : NotificationCaptureIntent
    data object OnTurnOffClick : NotificationCaptureIntent
    data object OnDeleteAllClick : NotificationCaptureIntent
    data object OnDeleteAllConfirm : NotificationCaptureIntent
    data object OnDeleteAllDismiss : NotificationCaptureIntent
}

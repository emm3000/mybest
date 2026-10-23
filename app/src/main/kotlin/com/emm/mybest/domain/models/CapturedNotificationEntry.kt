package com.emm.mybest.domain.models

/**
 * A [notification] with the label of the app it came from; the package name stands in when the app is no longer installed.
 */
data class CapturedNotificationEntry(
    val notification: CapturedNotification,
    val appLabel: String,
)

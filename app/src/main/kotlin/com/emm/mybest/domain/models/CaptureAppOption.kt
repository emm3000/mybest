package com.emm.mybest.domain.models

/**
 * An installed [app] with whether the user picked it for notification capture.
 */
data class CaptureAppOption(
    val app: InstalledApp,
    val isSelected: Boolean,
)

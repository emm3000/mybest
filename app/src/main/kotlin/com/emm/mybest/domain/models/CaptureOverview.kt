package com.emm.mybest.domain.models

/**
 * Everything the capture screen shows: every installed app with its selection, the captured [entries] newest first, and the [summary] of every selected app, installed or not.
 */
data class CaptureOverview(
    val apps: List<CaptureAppOption>,
    val entries: List<CapturedNotificationEntry>,
    val summary: CaptureSummary,
)

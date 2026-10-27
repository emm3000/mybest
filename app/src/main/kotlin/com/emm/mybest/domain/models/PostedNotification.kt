package com.emm.mybest.domain.models

import kotlin.time.Instant

/**
 * A notification just posted by the app with [packageName], before it is decided whether to capture it.
 * [isGroupSummary] marks the notification that only bundles other notifications of the same app.
 */
data class PostedNotification(
    val packageName: String,
    val title: String?,
    val text: String?,
    val postedAt: Instant,
    val isGroupSummary: Boolean = false,
)

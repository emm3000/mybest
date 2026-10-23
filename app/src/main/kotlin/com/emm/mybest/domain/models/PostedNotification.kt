package com.emm.mybest.domain.models

import kotlin.time.Instant

/**
 * A notification just posted by the app with [packageName], before it is decided whether to capture it.
 */
data class PostedNotification(
    val packageName: String,
    val title: String?,
    val text: String?,
    val postedAt: Instant,
)

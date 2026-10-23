package com.emm.mybest.domain.models

import kotlin.time.Instant

/**
 * A notification stored as received from the app with [packageName]: [title] and [text] are never parsed or modified.
 */
data class CapturedNotification(
    val id: Long,
    val packageName: String,
    val title: String?,
    val text: String?,
    val postedAt: Instant,
)

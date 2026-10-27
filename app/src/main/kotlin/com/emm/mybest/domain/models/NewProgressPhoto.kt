package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

/**
 * A photo to store. [createdAt] is the creation stamp in epoch milliseconds; null means now.
 */
data class NewProgressPhoto(
    val photoPath: String,
    val type: PhotoType,
    val date: LocalDate,
    val createdAt: Long? = null,
)

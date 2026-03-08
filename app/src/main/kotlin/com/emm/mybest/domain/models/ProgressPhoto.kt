package com.emm.mybest.domain.models

import java.time.LocalDate

data class ProgressPhoto(
    val id: String,
    val habitRecordId: String? = null,
    val date: LocalDate,
    val type: PhotoType,
    val photoPath: String,
    val createdAt: Long
)

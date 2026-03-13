package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.currentDate
import kotlinx.datetime.LocalDate

data class NewProgressPhoto(
    val photoPath: String,
    val type: PhotoType,
    val habitId: String? = null,
    val date: LocalDate = currentDate(),
)

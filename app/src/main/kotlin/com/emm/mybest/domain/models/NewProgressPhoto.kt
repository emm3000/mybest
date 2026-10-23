package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

data class NewProgressPhoto(
    val photoPath: String,
    val type: PhotoType,
    val date: LocalDate,
)

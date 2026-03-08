package com.emm.mybest.domain.models

import java.time.LocalDate

data class NewProgressPhoto(
    val photoPath: String,
    val type: PhotoType,
    val date: LocalDate = LocalDate.now(),
)

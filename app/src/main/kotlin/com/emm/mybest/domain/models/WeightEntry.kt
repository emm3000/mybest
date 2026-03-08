package com.emm.mybest.domain.models

import java.time.LocalDate

data class WeightEntry(
    val id: String,
    val date: LocalDate,
    val weight: Float,
    val photoPath: String? = null,
    val note: String? = null
)

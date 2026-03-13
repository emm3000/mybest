package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

data class WeightEntry(
    val id: String,
    val date: LocalDate,
    val weight: Float,
    val habitId: String? = null,
    val photoPath: String? = null,
    val note: String? = null,
)

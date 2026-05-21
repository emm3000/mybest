package com.emm.mybest.domain.usecase.history

import kotlinx.datetime.LocalDate

data class WeightTrendPoint(
    val date: LocalDate,
    val weight: Float,
)

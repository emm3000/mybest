package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

data class ExerciseCompliance(
    val date: LocalDate,
    val done: Boolean,
)

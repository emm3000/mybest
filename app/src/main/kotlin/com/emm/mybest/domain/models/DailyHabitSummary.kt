package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

data class DailyHabitSummary(
    val date: LocalDate,
    val ateHealthy: Boolean,
    val didExercise: Boolean,
    val notes: String?
)

package com.emm.mybest.domain.models

import java.time.LocalDate

data class DailyHabitSummary(
    val date: LocalDate,
    val ateHealthy: Boolean,
    val didExercise: Boolean,
    val notes: String?
)

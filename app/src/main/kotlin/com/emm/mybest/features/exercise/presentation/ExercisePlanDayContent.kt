package com.emm.mybest.features.exercise.presentation

import com.emm.mybest.domain.models.ExercisePlanEntry
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class ExercisePlanDayContent(
    val day: DayOfWeek,
    val today: LocalDate,
    val entry: ExercisePlanEntry,
)

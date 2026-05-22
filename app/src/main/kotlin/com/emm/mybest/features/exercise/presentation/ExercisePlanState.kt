package com.emm.mybest.features.exercise.presentation

import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.features.exercise.presentation.edit.EditingExerciseDraft
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class ExercisePlanState(
    val isLoading: Boolean = true,
    val today: LocalDate,
    val entries: Map<DayOfWeek, ExercisePlanEntry> = emptyMap(),
    val editing: EditingExerciseDraft? = null,
)

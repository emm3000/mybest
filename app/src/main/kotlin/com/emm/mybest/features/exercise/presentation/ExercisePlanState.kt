package com.emm.mybest.features.exercise.presentation

import kotlinx.datetime.DayOfWeek

data class ExercisePlanState(
    val isLoading: Boolean = true,
    val routines: Map<DayOfWeek, String> = emptyMap(),
    val editing: EditingExercise? = null,
)

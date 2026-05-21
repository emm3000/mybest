package com.emm.mybest.features.exercise.presentation

import kotlinx.datetime.DayOfWeek

data class EditingExercise(
    val day: DayOfWeek,
    val draftRoutine: String,
)

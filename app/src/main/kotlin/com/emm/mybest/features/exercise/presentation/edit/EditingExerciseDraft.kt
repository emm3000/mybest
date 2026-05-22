package com.emm.mybest.features.exercise.presentation.edit

import kotlinx.datetime.DayOfWeek

data class EditingExerciseDraft(
    val day: DayOfWeek,
    val name: String,
    val detail: String,
    val volume: String,
)

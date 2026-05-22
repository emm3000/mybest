package com.emm.mybest.features.exercise.presentation

import kotlinx.datetime.DayOfWeek

sealed interface ExercisePlanIntent {
    data class StartEdit(val day: DayOfWeek) : ExercisePlanIntent
    data class UpdateName(val name: String) : ExercisePlanIntent
    data class UpdateDetail(val detail: String) : ExercisePlanIntent
    data class UpdateVolume(val volume: String) : ExercisePlanIntent
    data object SaveRoutine : ExercisePlanIntent
    data object CancelEdit : ExercisePlanIntent
}

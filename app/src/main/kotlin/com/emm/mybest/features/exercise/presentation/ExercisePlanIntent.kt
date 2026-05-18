package com.emm.mybest.features.exercise.presentation

import kotlinx.datetime.DayOfWeek

sealed interface ExercisePlanIntent {
    data class StartEdit(val day: DayOfWeek) : ExercisePlanIntent
    data class UpdateDraft(val routine: String) : ExercisePlanIntent
    data object SaveRoutine : ExercisePlanIntent
    data object CancelEdit : ExercisePlanIntent
}

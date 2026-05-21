package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.MealType

sealed interface HomeIntent {
    data class ToggleMeal(val type: MealType, val done: Boolean) : HomeIntent
    data class ToggleExercise(val done: Boolean) : HomeIntent
}

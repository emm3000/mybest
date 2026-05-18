package com.emm.mybest.features.diet.presentation

import com.emm.mybest.domain.models.MealType
import kotlinx.datetime.DayOfWeek

sealed interface MealPlanIntent {
    data class StartEdit(val day: DayOfWeek, val type: MealType) : MealPlanIntent
    data class UpdateDraft(val description: String) : MealPlanIntent
    data object SaveMeal : MealPlanIntent
    data object CancelEdit : MealPlanIntent
}

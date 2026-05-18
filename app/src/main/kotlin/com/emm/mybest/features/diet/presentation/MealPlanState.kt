package com.emm.mybest.features.diet.presentation

import com.emm.mybest.domain.models.MealType
import kotlinx.datetime.DayOfWeek

data class MealPlanState(
    val isLoading: Boolean = true,
    val entries: Map<DayOfWeek, Map<MealType, String>> = emptyMap(),
    val editing: EditingMeal? = null,
)

data class EditingMeal(
    val day: DayOfWeek,
    val type: MealType,
    val draftDescription: String,
)

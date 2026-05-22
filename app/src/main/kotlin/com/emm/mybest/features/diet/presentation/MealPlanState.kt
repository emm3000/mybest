package com.emm.mybest.features.diet.presentation

import com.emm.mybest.domain.models.MealType
import com.emm.mybest.features.diet.presentation.edit.EditingMealDraft
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class MealPlanState(
    val isLoading: Boolean = true,
    val today: LocalDate,
    val entries: Map<DayOfWeek, Map<MealType, String>> = emptyMap(),
    val editing: EditingMealDraft? = null,
)

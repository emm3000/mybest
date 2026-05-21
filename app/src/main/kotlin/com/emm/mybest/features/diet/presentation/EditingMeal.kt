package com.emm.mybest.features.diet.presentation

import com.emm.mybest.domain.models.MealType
import kotlinx.datetime.DayOfWeek

data class EditingMeal(
    val day: DayOfWeek,
    val type: MealType,
    val draftDescription: String,
)

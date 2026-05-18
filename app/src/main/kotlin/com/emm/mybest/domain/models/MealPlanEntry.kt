package com.emm.mybest.domain.models

import kotlinx.datetime.DayOfWeek

data class MealPlanEntry(
    val dayOfWeek: DayOfWeek,
    val mealType: MealType,
    val description: String,
)

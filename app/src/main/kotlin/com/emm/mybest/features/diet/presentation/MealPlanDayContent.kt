package com.emm.mybest.features.diet.presentation

import com.emm.mybest.domain.models.MealType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

data class MealPlanDayContent(
    val day: DayOfWeek,
    val index: Int,
    val today: LocalDate,
    val meals: Map<MealType, String>,
)

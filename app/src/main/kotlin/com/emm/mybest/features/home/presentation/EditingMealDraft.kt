package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.MealType
import kotlinx.datetime.DayOfWeek

data class EditingMealDraft(
    val day: DayOfWeek,
    val slot: DailySlot,
    val type: MealType,
    val description: String,
)

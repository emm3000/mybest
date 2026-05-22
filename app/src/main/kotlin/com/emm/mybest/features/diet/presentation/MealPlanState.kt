package com.emm.mybest.features.diet.presentation

import com.emm.mybest.domain.models.MealType
import com.emm.mybest.features.diet.presentation.edit.EditingMealDraft
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class MealPlanState(
    val isLoading: Boolean = true,
    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val entries: Map<DayOfWeek, Map<MealType, String>> = emptyMap(),
    val editing: EditingMealDraft? = null,
)

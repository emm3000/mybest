package com.emm.mybest.domain.models

import kotlinx.datetime.DayOfWeek

data class WeeklyMealPlan(val entries: List<MealPlanEntry>) {
    fun forDay(day: DayOfWeek): List<MealPlanEntry> =
        entries.filter { it.dayOfWeek == day }.sortedBy { it.mealType.ordinal }

    fun entryFor(day: DayOfWeek, type: MealType): MealPlanEntry? =
        entries.firstOrNull { it.dayOfWeek == day && it.mealType == type }
}

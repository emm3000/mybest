package com.emm.mybest.domain.models

import kotlinx.datetime.DayOfWeek

data class WeeklyExercisePlan(val entries: List<ExercisePlanEntry>) {
    fun forDay(day: DayOfWeek): ExercisePlanEntry? =
        entries.firstOrNull { it.dayOfWeek == day }
}

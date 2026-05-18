package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

data class DailyCompliance(
    val date: LocalDate,
    val mealsDone: Map<MealType, Boolean>,
    val exerciseDone: Boolean,
) {
    val completionRatio: Float
        get() {
            val total = MealType.entries.size + 1
            val done = mealsDone.values.count { it } + if (exerciseDone) 1 else 0
            return done.toFloat() / total
        }
}

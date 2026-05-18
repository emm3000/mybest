package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

data class MealCompliance(
    val date: LocalDate,
    val mealType: MealType,
    val done: Boolean,
)

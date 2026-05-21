package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.MealType

data class MealRow(
    val type: MealType,
    val description: String,
    val done: Boolean,
)

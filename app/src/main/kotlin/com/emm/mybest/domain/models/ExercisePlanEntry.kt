package com.emm.mybest.domain.models

import kotlinx.datetime.DayOfWeek

data class ExercisePlanEntry(
    val dayOfWeek: DayOfWeek,
    val name: String = "",
    val detail: String = "",
    val volume: String = "",
)

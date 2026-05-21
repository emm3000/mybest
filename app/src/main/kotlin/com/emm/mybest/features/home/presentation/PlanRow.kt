package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailySlot
import kotlinx.datetime.LocalTime

data class PlanRow(
    val slot: DailySlot,
    val time: LocalTime,
    val description: String,
    val done: Boolean,
)

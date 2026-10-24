package com.emm.mybest.features.weight.presentation

import com.emm.mybest.domain.models.WeighInTrend
import kotlinx.datetime.DayOfWeek

data class AddWeightState(
    val weight: String = "",
    val note: String = "",
    val weightError: String? = null,
    val trend: WeighInTrend = WeighInTrend(),
    val targetWeightKg: Float? = null,
    val weighInDay: DayOfWeek? = null,
    val weighInDayLabel: String? = null,
    val isLoading: Boolean = false,
)

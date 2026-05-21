package com.emm.mybest.domain.usecase.history

import kotlinx.datetime.LocalDate

data class HistoryResult(
    val monthlyData: Map<LocalDate, DaySummary>,
    val weightTrend: List<WeightTrendPoint>,
    val streak: Int,
    val activeDays: Int,
)

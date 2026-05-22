package com.emm.mybest.domain.usecase.history

import kotlinx.datetime.LocalDate

data class HistoryResult(
    val monthlyData: Map<LocalDate, DaySummary>,
    val monthWeightCount: Int,
    val monthPhotoCount: Int,
    val recentEntries: List<HistoryRecentEntry>,
)

package com.emm.mybest.features.history.presentation

import kotlinx.datetime.LocalDate

data class HistoryRecentRowState(
    val date: LocalDate,
    val dateLabel: String,
    val description: HistoryRecentDescription,
    val isToday: Boolean,
)

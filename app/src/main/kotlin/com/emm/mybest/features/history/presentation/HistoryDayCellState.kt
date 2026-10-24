package com.emm.mybest.features.history.presentation

import kotlinx.datetime.LocalDate

internal data class HistoryDayCellState(
    val day: Int,
    val date: LocalDate,
    val marks: DayMarksState,
    val isToday: Boolean,
    val isFuture: Boolean,
)

package com.emm.mybest.features.history.presentation

import com.emm.mybest.core.datetime.YearMonthValue
import kotlinx.datetime.LocalDate

internal data class HistoryCalendarState(
    val month: YearMonthValue,
    val dayMarks: Map<LocalDate, DayMarksState>,
    val today: LocalDate,
)

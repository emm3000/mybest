package com.emm.mybest.features.history.presentation

import com.emm.mybest.core.datetime.YearMonthValue
import kotlinx.datetime.LocalDate

internal data class HistoryPagerCallbacks(
    val onMonthChange: (YearMonthValue) -> Unit,
    val onDateClick: (LocalDate) -> Unit,
    val onViewSelect: (HistoryView) -> Unit,
    val logbook: LogbookCallbacks,
)

package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.HistoryRange
import com.emm.mybest.domain.usecase.history.WeightTrendPoint
import kotlinx.datetime.LocalDate

@Stable
data class HistoryState(
    val selectedMonth: YearMonthValue = YearMonthValue.now(),
    val selectedRange: HistoryRange = HistoryRange.MONTH,
    val monthlyData: Map<LocalDate, DaySummary> = emptyMap(),
    val weightTrend: List<WeightTrendPoint> = emptyList(),
    val streak: Int = 0,
    val activeDays: Int = 0,
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

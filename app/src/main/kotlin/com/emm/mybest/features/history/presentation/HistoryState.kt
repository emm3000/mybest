package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.HistoryRecentEntry
import kotlinx.datetime.LocalDate

@Stable
data class HistoryState(
    val selectedMonth: YearMonthValue = YearMonthValue.now(),
    val monthlyData: Map<LocalDate, DaySummary> = emptyMap(),
    val monthWeightCount: Int = 0,
    val monthPhotoCount: Int = 0,
    val recentEntries: List<HistoryRecentEntry> = emptyList(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

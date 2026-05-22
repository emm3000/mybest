package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Stable
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.HistoryRecentEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate

@Stable
data class HistoryState(
    val selectedMonth: YearMonthValue = YearMonthValue.now(),
    val monthlyData: ImmutableMap<LocalDate, DaySummary> = persistentMapOf(),
    val monthWeightCount: Int = 0,
    val monthPhotoCount: Int = 0,
    val recentEntries: ImmutableList<HistoryRecentEntry> = persistentListOf(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

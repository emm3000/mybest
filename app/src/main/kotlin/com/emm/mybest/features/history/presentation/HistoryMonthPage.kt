package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.ui.components.atelier.Hairline
import kotlinx.datetime.LocalDate

@Composable
internal fun HistoryMonthPage(
    state: HistoryState,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        val monthData = remember(state.monthlyData, state.selectedMonth) {
            monthDataOnly(state.monthlyData, state.selectedMonth)
        }

        HistoryMonthHero(
            month = state.selectedMonth,
            weightCount = state.monthWeightCount,
            photoCount = state.monthPhotoCount,
        )
        Hairline()
        HistoryCalendarGrid(
            month = state.selectedMonth,
            monthlyData = monthData,
            today = today,
            onDateClick = onDateClick,
        )
        Hairline()
        HistoryLegend()
        Hairline()
        HistoryRecentEntries(
            entries = state.recentEntries,
            today = today,
            onRowClick = onDateClick,
        )
    }
}

private fun monthDataOnly(
    monthlyData: Map<LocalDate, DaySummary>,
    month: YearMonthValue,
): Map<LocalDate, DaySummary> = monthlyData.filterKeys { YearMonthValue.from(it) == month }

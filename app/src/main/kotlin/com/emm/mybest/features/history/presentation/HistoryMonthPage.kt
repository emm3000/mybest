package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        HistoryMonthHero(state = state)
        Hairline()
        HistoryCalendarGrid(
            calendar = HistoryCalendarState(
                month = state.selectedMonth,
                dayMarks = state.dayMarks,
                today = today,
            ),
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

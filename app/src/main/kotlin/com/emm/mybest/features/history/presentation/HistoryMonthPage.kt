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
    callbacks: HistoryPagerCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HistoryMonthHero(state = state)
        Hairline()
        HistoryViewSwitch(selected = state.selectedView, onSelect = callbacks.onViewSelect)
        Hairline()
        when (state.selectedView) {
            HistoryView.CALENDAR -> HistoryCalendarContent(state, today, callbacks.onDateClick)
            HistoryView.BITACORA -> BitacoraList(
                days = state.bitacoraDays,
                canWriteNote = state.canWriteNote,
                callbacks = callbacks.bitacora,
            )
        }
    }
}

@Composable
private fun HistoryCalendarContent(
    state: HistoryState,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
) {
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
}

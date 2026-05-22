package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierMonoFamily
import kotlinx.datetime.LocalDate

private val MONO_9_5 = 9.5.sp
private val TRACKING_018 = 0.18.em
private const val CALENDAR_COLUMNS = 7
private const val DOW_CYCLE = 7
private val WEEK_HEADERS = listOf("D", "L", "M", "X", "J", "V", "S")

internal data class CalendarCell(
    val day: Int?,
    val date: LocalDate?,
)

@Composable
internal fun HistoryCalendarGrid(
    month: YearMonthValue,
    monthlyData: Map<LocalDate, DaySummary>,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cells = remember(month) { buildCalendarCells(month) }
    val rows = cells.chunked(CALENDAR_COLUMNS)

    WeekHeaderRow()

    rows.forEach { rowCells ->
        CalendarRow(
            cells = rowCells,
            monthlyData = monthlyData,
            today = today,
            onDateClick = onDateClick,
            modifier = modifier
                .padding(horizontal = HISTORY_GUT)
                .padding(bottom = 2.dp),
        )
    }

    Box(modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
private fun CalendarRow(
    cells: List<CalendarCell>,
    monthlyData: Map<LocalDate, DaySummary>,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        cells.forEach { cell ->
            if (cell.day == null || cell.date == null) {
                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
            } else {
                HistoryDayCell(
                    day = cell.day,
                    date = cell.date,
                    summary = monthlyData[cell.date],
                    isToday = cell.date == today,
                    isFuture = cell.date > today,
                    onClick = onDateClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeekHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HISTORY_GUT)
            .padding(top = 12.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        WEEK_HEADERS.forEach { header ->
            Text(
                text = header,
                style = TextStyle(
                    fontFamily = AtelierMonoFamily,
                    fontSize = MONO_9_5,
                    color = AtelierInkTertiary,
                    letterSpacing = TRACKING_018,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun buildCalendarCells(month: YearMonthValue): List<CalendarCell> {
    val firstDay = month.atDay(1)
    val sundayOffset = (firstDay.dayOfWeek.ordinal + 1) % DOW_CYCLE
    val daysInMonth = month.lengthOfMonth()

    val cells = mutableListOf<CalendarCell>()
    repeat(sundayOffset) { cells.add(CalendarCell(day = null, date = null)) }
    for (day in 1..daysInMonth) {
        cells.add(CalendarCell(day = day, date = month.atDay(day)))
    }
    val remainder = cells.size % CALENDAR_COLUMNS
    if (remainder != 0) {
        repeat(CALENDAR_COLUMNS - remainder) { cells.add(CalendarCell(day = null, date = null)) }
    }
    return cells
}

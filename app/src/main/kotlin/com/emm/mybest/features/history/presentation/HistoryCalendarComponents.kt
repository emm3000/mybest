package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.datetime.currentDate
import com.emm.mybest.core.datetime.formatEsWeekdayDayMonth
import com.emm.mybest.core.datetime.narrowEs
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private const val CALENDAR_COLUMNS = 7
private const val DAY_CELL_CORNER = 12
private const val MONTH_COUNT = 12
private const val HALF_DIVISOR = 2
private const val YEAR_CELL_ASPECT = 0.6f

// Intensity is determined by activity count — single cyan gradient dimension.
internal enum class DayIntensity(val label: String) {
    NONE("Sin actividad"),
    LOW("Baja"),
    HIGH("Alta"),
}

internal fun resolveDayIntensity(summary: DaySummary?): DayIntensity {
    val score = listOfNotNull(
        summary?.hasWeight?.takeIf { it },
        summary?.hasPhoto?.takeIf { it },
    ).size

    return when (score) {
        0 -> DayIntensity.NONE
        1 -> DayIntensity.LOW
        else -> DayIntensity.HIGH
    }
}

@Composable
internal fun dayIntensityColor(intensity: DayIntensity, primary: Color): Color = when (intensity) {
    DayIntensity.NONE -> MaterialTheme.colorScheme.surfaceContainerHighest
    DayIntensity.LOW -> primary.copy(alpha = 0.25f)
    DayIntensity.HIGH -> primary.copy(alpha = 0.85f)
}

/** Weekday header row — only shown for MONTH range. */
@Composable
internal fun WeekdayHeaderRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        DayOfWeek.entries.forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.narrowEs(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Full month calendar grid — 7 columns with blank leading cells. */
@Composable
internal fun MonthCalendarGrid(
    yearMonth: YearMonthValue,
    dayData: Map<LocalDate, DaySummary>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { currentDate() },
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOffset = yearMonth.atDay(1).dayOfWeek.ordinal
    val totalCells = daysInMonth + firstDayOffset
    val weekCount = (totalCells + CALENDAR_COLUMNS - 1) / CALENDAR_COLUMNS

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(weekCount) { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(CALENDAR_COLUMNS) { col ->
                    val cellIndex = week * CALENDAR_COLUMNS + col
                    if (cellIndex < firstDayOffset || cellIndex >= totalCells) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val dayOfMonth = cellIndex - firstDayOffset + 1
                        val date = yearMonth.atDay(dayOfMonth)
                        HeatmapDayCell(
                            date = date,
                            summary = dayData[date],
                            today = today,
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/** Single-row heatmap for WEEK range — always 7 cells. */
@Composable
internal fun WeekHeatmapRow(
    dates: List<LocalDate>,
    dayData: Map<LocalDate, DaySummary>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { currentDate() },
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        dates.forEach { date ->
            HeatmapDayCell(
                date = date,
                summary = dayData[date],
                today = today,
                onClick = { onDateClick(date) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Single-row heatmap for YEAR range — 12 month cells. */
@Composable
internal fun YearHeatmapRow(
    yearMonth: YearMonthValue,
    dayData: Map<LocalDate, DaySummary>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val year = yearMonth.year
    val activityByMonth = remember(dayData, year) {
        (1..MONTH_COUNT).associate { month ->
            val ym = YearMonthValue(year, month)
            val count = (1..ym.lengthOfMonth()).count { dayData[ym.atDay(it)]?.hasActivity == true }
            month to count
        }
    }
    val months = remember(year) { (1..MONTH_COUNT).map { YearMonthValue(year, it) } }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        months.forEach { ym ->
            val activityCount = activityByMonth[ym.month] ?: 0
            val totalDays = ym.lengthOfMonth()
            val intensity = when {
                activityCount == 0 -> DayIntensity.NONE
                activityCount < totalDays / HALF_DIVISOR -> DayIntensity.LOW
                else -> DayIntensity.HIGH
            }
            val primary = MaterialTheme.colorScheme.primary
            val bgColor = dayIntensityColor(intensity, primary)
            val monthLabel = monthShortEs(ym.month)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(YEAR_CELL_ASPECT)
                    .clip(RoundedCornerShape(DAY_CELL_CORNER.dp))
                    .background(bgColor)
                    .clickable { onDateClick(ym.atDay(1)) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (intensity == DayIntensity.NONE) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                )
            }
        }
    }
}

private val MONTH_SHORT_LABELS = listOf("E", "F", "M", "A", "My", "J", "Jl", "Ag", "S", "O", "N", "D")

/** [month] is 1-based (1 = January). */
private fun monthShortEs(month: Int): String = MONTH_SHORT_LABELS.getOrElse(month - 1) { "?" }

@Composable
internal fun HeatmapDayCell(
    date: LocalDate,
    summary: DaySummary?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { currentDate() },
) {
    val isToday = date == today
    val primary = MaterialTheme.colorScheme.primary
    val intensity = resolveDayIntensity(summary)
    val backgroundColor = dayIntensityColor(intensity, primary)
    val contentDescription = buildDayCellDescription(date = date, summary = summary, isToday = isToday)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(DAY_CELL_CORNER.dp))
            .background(backgroundColor)
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) primary else Color.Transparent,
                shape = RoundedCornerShape(DAY_CELL_CORNER.dp),
            )
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

internal fun hasActivityInSelectedMonth(
    selectedMonth: YearMonthValue,
    monthlyData: Map<LocalDate, DaySummary>,
): Boolean = monthlyData.values.any { summary ->
    summary.hasActivity && YearMonthValue.from(summary.date) == selectedMonth
}

internal fun buildDayCellDescription(
    date: LocalDate,
    summary: DaySummary?,
    isToday: Boolean,
): String {
    val activitySummary = buildList {
        if (summary?.hasWeight == true) add("peso")
        if (summary?.hasPhoto == true) add("fotos")
    }.joinToString(", ")

    return buildString {
        val intensity = resolveDayIntensity(summary)
        if (isToday) {
            append("Hoy. ")
        }
        append(date.formatEsWeekdayDayMonth())
        append(". ")
        append("Intensidad: ${intensity.label.lowercase()}. ")
        if (activitySummary.isNotBlank()) {
            append("Actividad registrada: ")
            append(activitySummary)
        } else {
            append("Sin actividad registrada")
        }
        append(". Toca para ver el detalle del día.")
    }
}

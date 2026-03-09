package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.datetime.formatEsMonthYear
import com.emm.mybest.core.datetime.formatEsWeekdayDayMonth
import com.emm.mybest.core.datetime.shortEs
import com.emm.mybest.ui.components.HEmptyState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
internal fun HistoryMonthSection(
    selectedMonth: YearMonthValue,
    monthlyData: Map<LocalDate, DaySummary>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasActivityInSelectedMonth = monthlyData.values.any { summary ->
        summary.hasActivity && YearMonthValue.from(summary.date) == selectedMonth
    }

    if (hasActivityInSelectedMonth) {
        Row(modifier = modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.shortEs(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HistoryLegendSection()

        CalendarGrid(
            yearMonth = selectedMonth,
            onDateClick = onDateClick,
            dayData = monthlyData,
        )
    } else {
        HEmptyState(
            title = "Sin actividad en ${selectedMonth.formatEsMonthYear()}",
            description = "Cambia de mes o registra peso, hábitos o fotos para ver actividad diaria.",
            icon = Icons.Rounded.History,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun DayActivityIndicators(
    summary: DaySummary?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier.height(6.dp),
    ) {
        if (summary?.hasWeight == true) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        if (summary?.hasHabit == true) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
            )
        }
        if (summary?.hasPhoto == true) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
            )
        }
    }
}

internal fun buildDayCellDescription(
    date: LocalDate,
    summary: DaySummary?,
    isToday: Boolean,
): String {
    val activitySummary = buildList {
        if (summary?.hasWeight == true) add("peso")
        if (summary?.hasHabit == true) add("hábitos")
        if (summary?.hasPhoto == true) add("fotos")
    }.joinToString(", ")

    return buildString {
        if (isToday) {
            append("Hoy. ")
        }
        append(date.formatEsWeekdayDayMonth())
        append(". ")
        if (activitySummary.isNotBlank()) {
            append("Actividad registrada: ")
            append(activitySummary)
        } else {
            append("Sin actividad registrada")
        }
        append(". Toca para ver el detalle del día.")
    }
}

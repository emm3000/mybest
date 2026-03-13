package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.datetime.formatDdMmYy
import com.emm.mybest.core.datetime.formatEsMonthYear
import com.emm.mybest.core.datetime.formatEsWeekdayDayMonth
import com.emm.mybest.core.datetime.shortEs
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HStatChip
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

internal enum class DayIntensity(val label: String) {
    NONE("Sin actividad"),
    LOW("Baja"),
    MEDIUM("Media"),
    HIGH("Alta"),
}

internal fun resolveDayIntensity(summary: DaySummary?): DayIntensity {
    val score = listOfNotNull(
        summary?.hasWeight?.takeIf { it },
        summary?.hasHabit?.takeIf { it },
        summary?.hasPhoto?.takeIf { it },
    ).size

    return when (score) {
        0 -> DayIntensity.NONE
        1 -> DayIntensity.LOW
        2 -> DayIntensity.MEDIUM
        else -> DayIntensity.HIGH
    }
}

@Composable
internal fun dayIntensityColor(intensity: DayIntensity): Color = when (intensity) {
    DayIntensity.NONE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
    DayIntensity.LOW -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    DayIntensity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    DayIntensity.HIGH -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
}

@Composable
internal fun HistoryMonthSummarySection(
    summary: HistoryMonthSummary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryMetricChip(
            label = "Activos",
            value = summary.activityDays.toString(),
            modifier = Modifier.weight(1f),
        )
        SummaryMetricChip(
            label = "Peso",
            value = summary.weightDays.toString(),
            modifier = Modifier.weight(1f),
        )
        SummaryMetricChip(
            label = "Hábitos",
            value = summary.habitDays.toString(),
            modifier = Modifier.weight(1f),
        )
        SummaryMetricChip(
            label = "Fotos",
            value = summary.photoDays.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun HistoryWeekSummarySection(
    summary: HistoryWeekSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Resumen semanal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        val rangeText = if (summary.startDate != null && summary.endDate != null) {
            "Semana: ${summary.startDate.formatDdMmYy()} - ${summary.endDate.formatDdMmYy()}"
        } else {
            "Semana sin rango disponible"
        }
        Text(
            text = rangeText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SummaryMetricChip(
                label = "Activos",
                value = summary.activityDays.toString(),
                modifier = Modifier.weight(1f),
            )
            SummaryMetricChip(
                label = "Peso",
                value = summary.weightDays.toString(),
                modifier = Modifier.weight(1f),
            )
            SummaryMetricChip(
                label = "Hábitos",
                value = summary.habitDays.toString(),
                modifier = Modifier.weight(1f),
            )
            SummaryMetricChip(
                label = "Fotos",
                value = summary.photoDays.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    HStatChip(
        label = label,
        value = value,
        modifier = modifier,
    )
}

@Composable
internal fun HistoryMonthSection(
    selectedMonth: YearMonthValue,
    monthlyData: Map<LocalDate, DaySummary>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasActivityInSelectedMonth = hasActivityInSelectedMonth(
        selectedMonth = selectedMonth,
        monthlyData = monthlyData,
    )

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

internal fun hasActivityInSelectedMonth(
    selectedMonth: YearMonthValue,
    monthlyData: Map<LocalDate, DaySummary>,
): Boolean = monthlyData.values.any { summary ->
    summary.hasActivity && YearMonthValue.from(summary.date) == selectedMonth
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

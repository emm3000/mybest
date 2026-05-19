package com.emm.mybest.features.history.presentation

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.datetime.formatDdMmYy
import com.emm.mybest.core.datetime.formatEsMonthYear
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.HistoryRange
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal fun rangeLabel(month: YearMonthValue, range: HistoryRange): String = when (range) {
    HistoryRange.WEEK -> {
        val anchorDay = month.atDay(1)
        val weekStart = anchorDay.plus(DatePeriod(days = -anchorDay.dayOfWeek.ordinal))
        val weekEnd = weekStart.plus(DatePeriod(days = DAYS_IN_WEEK_LABEL - 1))
        "${weekStart.formatDdMmYy()} – ${weekEnd.formatDdMmYy()}"
    }
    HistoryRange.MONTH -> month.formatEsMonthYear()
    HistoryRange.YEAR -> month.year.toString()
}

internal fun findBestMonthName(
    monthlyData: Map<LocalDate, DaySummary>,
    year: Int,
): String {
    val spanishMonths = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
    )
    val bestMonthIndex = (1..MONTHS_IN_YEAR_LABEL)
        .maxByOrNull { month ->
            monthlyData.values.count { summary ->
                summary.hasActivity &&
                    summary.date.year == year &&
                    summary.date.month.ordinal + 1 == month
            }
        } ?: 1
    return spanishMonths.getOrElse(bestMonthIndex - 1) { "enero" }
        .replaceFirstChar { it.uppercase() }
}

private const val DAYS_IN_WEEK_LABEL = 7
private const val MONTHS_IN_YEAR_LABEL = 12

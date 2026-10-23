package com.emm.mybest.features.summary.presentation

import com.emm.mybest.core.datetime.MONTH_ABBR_ES
import com.emm.mybest.domain.models.WeeklySummary
import kotlinx.datetime.LocalDate

@Suppress("UnusedReceiverParameter")
internal fun WeeklySummary.toState(week: SummaryWeek): SummaryState = SummaryState(
    weekLabel = "${week.range.start.toWeekDayLabel()} – ${week.range.endInclusive.toWeekDayLabel()}",
    canGoNext = !week.isCurrent,
)

private fun LocalDate.toWeekDayLabel(): String = "$day ${MONTH_ABBR_ES.getValue(month.ordinal + 1)}"

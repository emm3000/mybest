package com.emm.mybest.features.summary.presentation

import com.emm.mybest.core.datetime.appWeekOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

private const val DAYS_PER_WEEK = 7

internal data class SummaryWeek(
    val today: LocalDate,
    val weeksBack: Int,
) {
    val appDay: LocalDate = today.minus(weeksBack * DAYS_PER_WEEK, DateTimeUnit.DAY)
    val range: ClosedRange<LocalDate> = appWeekOf(appDay)
    val isCurrent: Boolean = weeksBack == 0
}

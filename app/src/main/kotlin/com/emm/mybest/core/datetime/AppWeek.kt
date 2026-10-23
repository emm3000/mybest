package com.emm.mybest.core.datetime

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private const val DAYS_AFTER_MONDAY = 6

/**
 * Returns the Monday..Sunday week of app days that contains [appDay].
 */
fun appWeekOf(appDay: LocalDate): ClosedRange<LocalDate> {
    val monday = appDay.minus(appDay.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    return monday..monday.plus(DAYS_AFTER_MONDAY, DateTimeUnit.DAY)
}

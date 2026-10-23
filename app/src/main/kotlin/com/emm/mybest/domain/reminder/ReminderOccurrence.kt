package com.emm.mybest.domain.reminder

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

private const val DAYS_IN_WEEK = 7

/**
 * Returns the first moment strictly after [from] that falls on one of [days] at [time].
 *
 * [days] must not be empty.
 */
fun nextOccurrence(from: LocalDateTime, days: Set<DayOfWeek>, time: LocalTime): LocalDateTime =
    (0..DAYS_IN_WEEK)
        .map { offset -> LocalDateTime(from.date.plus(offset, DateTimeUnit.DAY), time) }
        .first { candidate -> candidate > from && candidate.date.dayOfWeek in days }

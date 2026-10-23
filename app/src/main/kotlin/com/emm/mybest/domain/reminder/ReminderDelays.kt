package com.emm.mybest.domain.reminder

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * Measures how long a reminder has to wait for its next occurrence.
 */
class ReminderDelays(
    private val clock: Clock,
    private val timeZone: TimeZone,
) {

    /**
     * The time from now until the next [time] on one of [days], in the local time zone.
     */
    fun untilNext(days: Set<DayOfWeek>, time: LocalTime): Duration {
        val now = clock.now()
        val next = nextOccurrence(now.toLocalDateTime(timeZone), days, time)
        return next.toInstant(timeZone) - now
    }
}

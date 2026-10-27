package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime

private const val EVENING_START_HOUR = 18
private const val SECONDS_PER_MINUTE = 60

/**
 * The local time from which the evening begins, up to [APP_DAY_START].
 */
val EVENING_START: LocalTime = LocalTime(EVENING_START_HOUR, 0)

/**
 * Minutes elapsed since [EVENING_START], in `0 until 1440`; times after midnight wrap onto the same evening, so 00:15
 * comes after 23:30 and 17:59 is the last minute of the timeline.
 */
fun LocalTime.minutesIntoEvening(): Int {
    val minutesSinceStart = (toSecondOfDay() - EVENING_START.toSecondOfDay()) / SECONDS_PER_MINUTE
    return minutesSinceStart.mod(MINUTES_PER_DAY)
}

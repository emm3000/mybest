package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime

private const val APP_DAY_START_HOUR = 5
private const val SECONDS_PER_MINUTE = 60
private const val MINUTES_PER_DAY = 24 * 60

/**
 * The local time at which every app day begins and the previous one ends (ADR 0003).
 */
val APP_DAY_START: LocalTime = LocalTime(APP_DAY_START_HOUR, 0)

/**
 * Minutes elapsed since [APP_DAY_START], in `0 until 1440`; times after midnight and before the start wrap onto the
 * same app day, so 00:15 comes after 23:30.
 */
fun LocalTime.minutesIntoAppDay(): Int {
    val minutesSinceStart = (toSecondOfDay() - APP_DAY_START.toSecondOfDay()) / SECONDS_PER_MINUTE
    return minutesSinceStart.mod(MINUTES_PER_DAY)
}

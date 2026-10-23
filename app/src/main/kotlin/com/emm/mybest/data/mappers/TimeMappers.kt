package com.emm.mybest.data.mappers

import kotlinx.datetime.LocalTime

private const val SECONDS_PER_MINUTE = 60

internal fun LocalTime.toMinutesSinceMidnight(): Int = toSecondOfDay() / SECONDS_PER_MINUTE

internal fun Int.toLocalTime(): LocalTime = LocalTime.fromSecondOfDay(this * SECONDS_PER_MINUTE)

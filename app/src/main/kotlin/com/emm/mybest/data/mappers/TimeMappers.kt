package com.emm.mybest.data.mappers

import com.emm.mybest.core.datetime.SECONDS_PER_MINUTE
import kotlinx.datetime.LocalTime

internal fun LocalTime.toMinutesSinceMidnight(): Int = toSecondOfDay() / SECONDS_PER_MINUTE

internal fun Int.toLocalTime(): LocalTime = LocalTime.fromSecondOfDay(this * SECONDS_PER_MINUTE)

package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime

private const val EVENING_START_HOUR = 18

/**
 * The local time from which the evening begins, up to [APP_DAY_START].
 */
val EVENING_START: LocalTime = LocalTime(EVENING_START_HOUR, 0)

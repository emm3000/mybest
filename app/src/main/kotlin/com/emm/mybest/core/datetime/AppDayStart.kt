package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime

private const val APP_DAY_START_HOUR = 5

/**
 * The local time at which every app day begins and the previous one ends (ADR 0003).
 */
val APP_DAY_START: LocalTime = LocalTime(APP_DAY_START_HOUR, 0)

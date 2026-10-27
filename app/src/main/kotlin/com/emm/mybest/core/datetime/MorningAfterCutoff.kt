package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime

/**
 * The local time that ends the morning, which begins at [APP_DAY_START].
 */
val MORNING_AFTER_CUTOFF: LocalTime = LocalTime(12, 0)

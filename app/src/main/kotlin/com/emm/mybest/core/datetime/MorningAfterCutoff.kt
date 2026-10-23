package com.emm.mybest.core.datetime

import kotlinx.datetime.LocalTime

/**
 * The local time that separates a return home the morning after an outing (from [APP_DAY_START] up to this time) from
 * a return the same evening or night (this time up to [APP_DAY_START]).
 */
val MORNING_AFTER_CUTOFF: LocalTime = LocalTime(12, 0)

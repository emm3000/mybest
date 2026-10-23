package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

/**
 * A yes/no mark for one [appDay] on one [type] of daily check.
 */
data class DailyCheck(
    val appDay: LocalDate,
    val type: DailyCheckType,
    val isDone: Boolean,
)

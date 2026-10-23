package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

/**
 * A yes/no mark for one [appDay] on one [type] of daily check.
 */
data class DailyCheck(
    val appDay: LocalDate,
    val type: DailyCheckType,
    val isDone: Boolean,
) {
    /**
     * Whether this mark counts as a kept target: gym, diet or English done, or alcohol recorded as not drank. A check
     * with no mark is not kept.
     */
    fun isKept(): Boolean = if (type == DailyCheckType.ALCOHOL) !isDone else isDone
}

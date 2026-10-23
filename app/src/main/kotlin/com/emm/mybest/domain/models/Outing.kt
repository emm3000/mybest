package com.emm.mybest.domain.models

import kotlinx.datetime.LocalDate

/**
 * A night out on [appDay], either planned with [limits] or unplanned without them. [checkIn] is null until the
 * next-day check-in is saved.
 */
data class Outing(
    val id: Long,
    val appDay: LocalDate,
    val kind: OutingKind,
    val limits: Limits?,
    val checkIn: CheckIn?,
) {
    val isPendingCheckIn: Boolean get() = checkIn == null

    /**
     * Whether any limit was exceeded; an unplanned outing or one still pending its check-in has broken none.
     */
    val hasBrokenLimit: Boolean get() = limitResults().any { it.exceededBy > 0 }

    /**
     * How far each limit was exceeded, or an empty list for an unplanned outing or one still pending its check-in.
     */
    fun limitResults(): List<LimitResult> {
        if (kind == OutingKind.UNPLANNED || limits == null || checkIn == null) return emptyList()
        return limits.evaluate(checkIn)
    }
}

package com.emm.mybest.domain.models

import kotlinx.datetime.LocalTime

/**
 * Configured time of day for each daily slot. Global (not per-day) — editing
 * once propagates to every weekday's row. Any slot the user has not customised
 * falls back to [DEFAULT_TIMES].
 */
data class DailySlotTimes(val times: Map<DailySlot, LocalTime>) {
    operator fun get(slot: DailySlot): LocalTime = times[slot] ?: DEFAULT_TIMES.getValue(slot)

    companion object {
        val DEFAULT_TIMES: Map<DailySlot, LocalTime> = mapOf(
            DailySlot.BREAKFAST to LocalTime(7, 30),
            DailySlot.LUNCH to LocalTime(13, 30),
            DailySlot.SNACK to LocalTime(17, 0),
            DailySlot.DINNER to LocalTime(20, 0),
            DailySlot.EXERCISE to LocalTime(18, 30),
        )
    }
}

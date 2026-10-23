package com.emm.mybest.domain.reminder

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

/**
 * Schedules the weigh-in reminder: every week on the weigh-in day at the chosen time.
 */
interface WeightReminderScheduler {
    /**
     * Schedules the reminder on [day] at [time]. Keeps an unfinished schedule for the same day, time and time zone;
     * otherwise replaces it.
     */
    suspend fun schedule(day: DayOfWeek, time: LocalTime)

    /**
     * Cancels the reminder; does nothing when none is scheduled.
     */
    suspend fun cancel()
}

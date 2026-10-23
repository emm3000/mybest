package com.emm.mybest.domain.reminder

import kotlinx.datetime.LocalTime

/**
 * Schedules and cancels one recurring reminder.
 */
interface ReminderScheduler {
    /**
     * Schedules the reminder at [time]. Keeps an unfinished schedule for the same time and time zone; otherwise
     * replaces it.
     */
    suspend fun schedule(time: LocalTime)

    /**
     * Cancels the reminder; does nothing when none is scheduled.
     */
    suspend fun cancel()
}

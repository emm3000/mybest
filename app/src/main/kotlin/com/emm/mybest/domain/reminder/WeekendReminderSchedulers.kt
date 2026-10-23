package com.emm.mybest.domain.reminder

/**
 * The reminders that frame a weekend outing: the one before it and the check-in after it.
 */
data class WeekendReminderSchedulers(
    val preOuting: PreOutingReminderScheduler,
    val checkIn: CheckInReminderScheduler,
)

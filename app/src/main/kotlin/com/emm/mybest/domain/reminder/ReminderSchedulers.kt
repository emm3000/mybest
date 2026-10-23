package com.emm.mybest.domain.reminder

/**
 * Every recurring reminder the setup schedules: the [weekend] pair and the [weighIn] reminder.
 */
data class ReminderSchedulers(
    val weekend: WeekendReminderSchedulers,
    val weighIn: WeightReminderScheduler,
)

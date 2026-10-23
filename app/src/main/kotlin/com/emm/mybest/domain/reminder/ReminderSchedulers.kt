package com.emm.mybest.domain.reminder

/**
 * Every recurring reminder the setup schedules: the [weekend] pair, the [weighIn] reminder and the [debt] reminder.
 */
data class ReminderSchedulers(
    val weekend: WeekendReminderSchedulers,
    val weighIn: WeightReminderScheduler,
    val debt: DebtReminderScheduler,
)

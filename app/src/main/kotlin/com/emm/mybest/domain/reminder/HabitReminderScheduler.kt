package com.emm.mybest.domain.reminder

import com.emm.mybest.domain.models.Habit

interface HabitReminderScheduler {
    suspend fun schedulePreventiveReminder(habit: Habit)
    suspend fun cancelReminder(habitId: String)
}

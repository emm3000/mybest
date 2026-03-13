package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.reminder.HabitReminderScheduler
import com.emm.mybest.domain.repository.HabitRepository

class UpdateHabitUseCase(
    private val habitRepository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler,
) {
    suspend operator fun invoke(habit: Habit) {
        habitRepository.updateHabit(habit)
        if (habit.isEnabled) {
            reminderScheduler.schedulePreventiveReminder(habit)
        } else {
            reminderScheduler.cancelReminder(habit.id)
        }
    }
}

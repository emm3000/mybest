package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.reminder.HabitReminderScheduler
import com.emm.mybest.domain.repository.HabitRepository

class CreateHabitUseCase(
    private val habitRepository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler,
) {
    suspend operator fun invoke(habit: Habit) {
        habitRepository.insertHabit(habit)
        if (habit.isEnabled) {
            reminderScheduler.schedulePreventiveReminder(habit)
        } else {
            reminderScheduler.cancelReminder(habit.id)
        }
    }
}

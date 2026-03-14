package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.reminder.HabitReminderScheduler
import com.emm.mybest.domain.repository.HabitRepository
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class UpdateDefaultReminderTimeUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val habitRepository: HabitRepository,
    private val reminderScheduler: HabitReminderScheduler,
) {
    suspend operator fun invoke(hour: Int, minute: Int) {
        userPreferencesRepository.updateDefaultReminderTime(hour, minute)

        val habits = habitRepository.getAllHabits().first()
        habits
            .asSequence()
            .filter { it.reminderHour == null && it.reminderMinute == null }
            .forEach { reminderScheduler.schedulePreventiveReminder(it) }
    }
}

package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.repository.HabitRepository

class UpdateHabitUseCase(
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(habit: Habit) {
        habitRepository.updateHabit(habit)
    }
}

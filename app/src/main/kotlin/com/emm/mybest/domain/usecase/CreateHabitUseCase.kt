package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.repository.HabitRepository

class CreateHabitUseCase(
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(habit: Habit) {
        habitRepository.insertHabit(habit)
    }
}

package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first

class GetHabitByIdUseCase(
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(habitId: String): Habit? {
        return habitRepository.getHabitById(habitId).first()
    }
}

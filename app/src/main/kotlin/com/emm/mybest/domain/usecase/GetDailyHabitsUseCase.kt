package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class GetDailyHabitsUseCase(
    private val habitRepository: HabitRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<HabitWithRecord>> {
        return habitRepository.getHabitsWithRecordsForDate(date)
    }
}

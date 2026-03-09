package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.repository.HabitRepository
import kotlinx.datetime.LocalDate
import java.util.UUID

class ToggleHabitUseCase(
    private val habitRepository: HabitRepository,
) {
    suspend operator fun invoke(habitWithRecord: HabitWithRecord, date: LocalDate) {
        val currentRecord = habitWithRecord.record
        val newRecord = if (currentRecord == null) {
            HabitRecord(
                id = UUID.randomUUID().toString(),
                habitId = habitWithRecord.habit.id,
                date = date,
                value = 1f,
                isCompleted = true,
            )
        } else {
            currentRecord.copy(
                isCompleted = !currentRecord.isCompleted,
                value = if (!currentRecord.isCompleted) 1f else 0f,
            )
        }
        habitRepository.insertRecord(newRecord)
    }
}

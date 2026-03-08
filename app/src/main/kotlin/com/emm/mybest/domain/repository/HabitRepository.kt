package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitById(id: String): Flow<Habit?>
    fun getHabitsWithRecordsForDate(date: java.time.LocalDate): Flow<List<com.emm.mybest.domain.models.HabitWithRecord>>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun insertRecord(record: com.emm.mybest.domain.models.HabitRecord)
}

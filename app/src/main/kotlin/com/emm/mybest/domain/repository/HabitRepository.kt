package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitWithRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getHabitById(id: String): Flow<Habit?>
    fun getHabitsWithRecordsForDate(date: LocalDate): Flow<List<HabitWithRecord>>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun insertRecord(record: HabitRecord)
}

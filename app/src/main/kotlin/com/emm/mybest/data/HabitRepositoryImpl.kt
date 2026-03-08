package com.emm.mybest.data

import com.emm.mybest.data.entities.HabitDao
import com.emm.mybest.data.entities.HabitRecordDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val habitRecordDao: HabitRecordDao,
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHabitById(id: String): Flow<Habit?> {
        return habitDao.getHabitById(id).map { it?.toDomain() }
    }

    override fun getHabitsWithRecordsForDate(date: java.time.LocalDate): Flow<List<HabitWithRecord>> {
        return combine(
            habitDao.getAllEnabledHabits(),
            habitRecordDao.getRecordsByDate(date),
        ) { habits, records ->
            habits.map { habitEntity ->
                val recordEntity = records.find { it.habitId == habitEntity.id }
                HabitWithRecord(
                    habit = habitEntity.toDomain(),
                    record = recordEntity?.toDomain(),
                )
            }
        }
    }

    override suspend fun insertHabit(habit: Habit) {
        habitDao.insert(habit.toEntity())
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.update(habit.toEntity())
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.delete(habit.toEntity())
    }

    override suspend fun insertRecord(record: HabitRecord) {
        habitRecordDao.insert(record.toEntity())
    }
}

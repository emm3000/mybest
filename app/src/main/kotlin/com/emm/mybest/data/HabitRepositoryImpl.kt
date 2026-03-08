package com.emm.mybest.data

import com.emm.mybest.data.entities.HabitDao
import com.emm.mybest.data.entities.HabitEntity
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import com.emm.mybest.data.entities.HabitType as DataHabitType

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val habitRecordDao: com.emm.mybest.data.entities.HabitRecordDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHabitById(id: String): Flow<Habit?> {
        return habitDao.getHabitById(id).map { it?.toDomain() }
    }

    override fun getHabitsWithRecordsForDate(date: java.time.LocalDate): Flow<List<com.emm.mybest.domain.models.HabitWithRecord>> {
        return kotlinx.coroutines.flow.combine(
            habitDao.getAllEnabledHabits(),
            habitRecordDao.getRecordsByDate(date)
        ) { habits, records ->
            habits.map { habitEntity ->
                val recordEntity = records.find { it.habitId == habitEntity.id }
                com.emm.mybest.domain.models.HabitWithRecord(
                    habit = habitEntity.toDomain(),
                    record = recordEntity?.toDomain()
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

    override suspend fun insertRecord(record: com.emm.mybest.domain.models.HabitRecord) {
        habitRecordDao.insert(record.toEntity())
    }
}

// Mappers
fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    icon = icon,
    color = color,
    category = category,
    type = when (type) {
        DataHabitType.BOOLEAN -> HabitType.BOOLEAN
        DataHabitType.TIME -> HabitType.TIME
        DataHabitType.METRIC -> HabitType.METRIC
    },
    goalValue = goalValue,
    unit = unit,
    isEnabled = isEnabled,
    scheduledDays = scheduledDays.split(",").filter { it.isNotBlank() }.map { DayOfWeek.of(it.toInt()) }.toSet(),
    createdAt = createdAt
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    category = category,
    type = when (type) {
        HabitType.BOOLEAN -> DataHabitType.BOOLEAN
        HabitType.TIME -> DataHabitType.TIME
        HabitType.METRIC -> DataHabitType.METRIC
    },
    goalValue = goalValue,
    unit = unit,
    isEnabled = isEnabled,
    scheduledDays = scheduledDays.joinToString(",") { it.value.toString() },
    createdAt = createdAt
)

fun com.emm.mybest.data.entities.HabitRecordEntity.toDomain(): com.emm.mybest.domain.models.HabitRecord =
    com.emm.mybest.domain.models.HabitRecord(
        id = id,
        habitId = habitId,
        date = date,
        value = value,
        isCompleted = isCompleted,
        notes = notes
    )

fun com.emm.mybest.domain.models.HabitRecord.toEntity(): com.emm.mybest.data.entities.HabitRecordEntity =
    com.emm.mybest.data.entities.HabitRecordEntity(
        id = id,
        habitId = habitId,
        date = date,
        value = value,
        isCompleted = isCompleted,
        notes = notes
    )

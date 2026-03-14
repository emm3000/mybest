package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.HabitEntity
import com.emm.mybest.data.entities.HabitRecordEntity
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitType
import kotlinx.datetime.DayOfWeek
import com.emm.mybest.data.entities.HabitType as DataHabitType

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
    reminderEnabled = reminderEnabled,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    scheduledDays = scheduledDays
        .split(",")
        .filter { it.isNotBlank() }
        .map { value ->
            value.toIntOrNull()?.let { iso ->
                DayOfWeek.entries.getOrNull(iso - 1)
            } ?: DayOfWeek.valueOf(value)
        }
        .toSet(),
    createdAt = createdAt,
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
    reminderEnabled = reminderEnabled,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    scheduledDays = scheduledDays.joinToString(",") { (it.ordinal + 1).toString() },
    createdAt = createdAt,
)

fun HabitRecordEntity.toDomain(): HabitRecord = HabitRecord(
    id = id,
    habitId = habitId,
    date = date,
    value = value,
    isCompleted = isCompleted,
    notes = notes,
)

fun HabitRecord.toEntity(): HabitRecordEntity = HabitRecordEntity(
    id = id,
    habitId = habitId,
    date = date,
    value = value,
    isCompleted = isCompleted,
    notes = notes,
)

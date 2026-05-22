package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.ExercisePlanEntryEntity
import com.emm.mybest.domain.models.ExercisePlanEntry
import kotlinx.datetime.DayOfWeek

fun ExercisePlanEntryEntity.toDomain(): ExercisePlanEntry? {
    val day = runCatching { DayOfWeek.valueOf(dayOfWeek) }.getOrNull() ?: return null
    return ExercisePlanEntry(dayOfWeek = day, name = name, detail = detail, volume = volume)
}

fun ExercisePlanEntry.toEntity(): ExercisePlanEntryEntity = ExercisePlanEntryEntity(
    dayOfWeek = dayOfWeek.name,
    name = name,
    detail = detail,
    volume = volume,
)

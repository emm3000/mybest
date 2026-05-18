package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.MealPlanEntryEntity
import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.MealType
import kotlinx.datetime.DayOfWeek

fun MealPlanEntryEntity.toDomain(): MealPlanEntry? {
    val day = runCatching { DayOfWeek.valueOf(dayOfWeek) }.getOrNull() ?: return null
    val type = runCatching { MealType.valueOf(mealType) }.getOrNull() ?: return null
    return MealPlanEntry(dayOfWeek = day, mealType = type, description = description)
}

fun MealPlanEntry.toEntity(): MealPlanEntryEntity = MealPlanEntryEntity(
    dayOfWeek = dayOfWeek.name,
    mealType = mealType.name,
    description = description,
)

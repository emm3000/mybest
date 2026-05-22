package com.emm.mybest.data.mappers

import com.emm.mybest.data.entities.ExerciseComplianceEntity
import com.emm.mybest.data.entities.MealComplianceEntity
import com.emm.mybest.domain.models.ExerciseCompliance
import com.emm.mybest.domain.models.MealCompliance
import com.emm.mybest.domain.models.MealType

fun MealComplianceEntity.toDomain(): MealCompliance? {
    val type = runCatching { MealType.valueOf(mealType) }.getOrNull() ?: return null
    return MealCompliance(date = date, mealType = type, done = done)
}

fun ExerciseComplianceEntity.toDomain(): ExerciseCompliance =
    ExerciseCompliance(date = date, done = done)

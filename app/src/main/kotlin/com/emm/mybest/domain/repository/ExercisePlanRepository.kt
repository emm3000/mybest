package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.domain.models.WeeklyExercisePlan
import kotlinx.coroutines.flow.Flow

interface ExercisePlanRepository {
    fun observePlan(): Flow<WeeklyExercisePlan>
    suspend fun upsertEntry(entry: ExercisePlanEntry)
    suspend fun clear()
}

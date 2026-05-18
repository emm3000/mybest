package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.WeeklyMealPlan
import kotlinx.coroutines.flow.Flow

interface MealPlanRepository {
    fun observePlan(): Flow<WeeklyMealPlan>
    suspend fun upsertEntry(entry: MealPlanEntry)
    suspend fun clear()
}

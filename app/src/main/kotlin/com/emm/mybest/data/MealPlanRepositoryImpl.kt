package com.emm.mybest.data

import com.emm.mybest.data.entities.MealPlanDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.WeeklyMealPlan
import com.emm.mybest.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MealPlanRepositoryImpl(private val dao: MealPlanDao) : MealPlanRepository {
    override fun observePlan(): Flow<WeeklyMealPlan> =
        dao.observeAll().map { rows ->
            WeeklyMealPlan(rows.mapNotNull { it.toDomain() })
        }

    override suspend fun upsertEntry(entry: MealPlanEntry) = dao.upsert(entry.toEntity())

    override suspend fun clear() = dao.clear()
}

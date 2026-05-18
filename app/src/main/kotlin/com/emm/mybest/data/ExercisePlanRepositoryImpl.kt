package com.emm.mybest.data

import com.emm.mybest.data.entities.ExercisePlanDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.domain.models.WeeklyExercisePlan
import com.emm.mybest.domain.repository.ExercisePlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExercisePlanRepositoryImpl(private val dao: ExercisePlanDao) : ExercisePlanRepository {
    override fun observePlan(): Flow<WeeklyExercisePlan> =
        dao.observeAll().map { rows ->
            WeeklyExercisePlan(rows.mapNotNull { it.toDomain() })
        }

    override suspend fun upsertEntry(entry: ExercisePlanEntry) = dao.upsert(entry.toEntity())

    override suspend fun clear() = dao.clear()
}

package com.emm.mybest.data

import com.emm.mybest.data.entities.ExerciseComplianceDao
import com.emm.mybest.data.entities.ExerciseComplianceEntity
import com.emm.mybest.data.entities.MealComplianceDao
import com.emm.mybest.data.entities.MealComplianceEntity
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.domain.models.ExerciseCompliance
import com.emm.mybest.domain.models.MealCompliance
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.repository.ComplianceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class ComplianceRepositoryImpl(
    private val mealDao: MealComplianceDao,
    private val exerciseDao: ExerciseComplianceDao,
) : ComplianceRepository {
    override fun observeMealsByDate(date: LocalDate): Flow<List<MealCompliance>> =
        mealDao.observeByDate(date.toString()).map { it.mapNotNull(MealComplianceEntity::toDomain) }

    override fun observeExerciseByDate(date: LocalDate): Flow<ExerciseCompliance?> =
        exerciseDao.observeByDate(date.toString()).map { it?.toDomain() }

    override fun observeMealsRange(from: LocalDate, to: LocalDate): Flow<List<MealCompliance>> =
        mealDao.observeRange(from.toString(), to.toString())
            .map { it.mapNotNull(MealComplianceEntity::toDomain) }

    override fun observeExerciseRange(from: LocalDate, to: LocalDate): Flow<List<ExerciseCompliance>> =
        exerciseDao.observeRange(from.toString(), to.toString())
            .map { rows -> rows.mapNotNull { it.toDomain() } }

    override suspend fun toggleMeal(date: LocalDate, type: MealType, done: Boolean) =
        mealDao.upsert(MealComplianceEntity(date.toString(), type.name, done))

    override suspend fun toggleExercise(date: LocalDate, done: Boolean) =
        exerciseDao.upsert(ExerciseComplianceEntity(date.toString(), done))
}

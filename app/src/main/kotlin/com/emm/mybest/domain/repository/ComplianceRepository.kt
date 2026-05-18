package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.ExerciseCompliance
import com.emm.mybest.domain.models.MealCompliance
import com.emm.mybest.domain.models.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ComplianceRepository {
    fun observeMealsByDate(date: LocalDate): Flow<List<MealCompliance>>
    fun observeExerciseByDate(date: LocalDate): Flow<ExerciseCompliance?>
    fun observeMealsRange(from: LocalDate, to: LocalDate): Flow<List<MealCompliance>>
    fun observeExerciseRange(from: LocalDate, to: LocalDate): Flow<List<ExerciseCompliance>>
    suspend fun toggleMeal(date: LocalDate, type: MealType, done: Boolean)
    suspend fun toggleExercise(date: LocalDate, done: Boolean)
}

package com.emm.mybest.domain.usecase.compliance

import com.emm.mybest.domain.models.ExerciseCompliance
import com.emm.mybest.domain.models.MealCompliance
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.repository.ComplianceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetCompletionStreakUseCase(
    private val repository: ComplianceRepository,
) {
    operator fun invoke(today: LocalDate): Flow<Int> {
        val from = today.minus(DatePeriod(days = MAX_LOOKBACK_DAYS))
        return combine(
            repository.observeMealsRange(from, today),
            repository.observeExerciseRange(from, today),
        ) { meals, exercises -> countStreak(today, meals, exercises) }
    }

    private fun countStreak(
        today: LocalDate,
        meals: List<MealCompliance>,
        exercises: List<ExerciseCompliance>,
    ): Int {
        val completed = completedDates(meals, exercises)
        var count = 0
        var cursor = if (today in completed) today else today.minus(DatePeriod(days = 1))
        while (cursor in completed) {
            count++
            cursor = cursor.minus(DatePeriod(days = 1))
        }
        return count
    }

    private fun completedDates(
        meals: List<MealCompliance>,
        exercises: List<ExerciseCompliance>,
    ): Set<LocalDate> {
        val exerciseDates = exercises.filter { it.done }.map { it.date }.toSet()
        val requiredMeals = MealType.entries.size
        val mealDates = meals.filter { it.done }
            .groupBy { it.date }
            .filterValues { rows -> rows.map { it.mealType }.toSet().size == requiredMeals }
            .keys
        return mealDates.intersect(exerciseDates)
    }

    private companion object {
        const val MAX_LOOKBACK_DAYS = 365
    }
}

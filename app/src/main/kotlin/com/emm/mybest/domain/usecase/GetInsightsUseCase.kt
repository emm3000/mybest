package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.InsightsData
import com.emm.mybest.domain.repository.DailyHabitRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetInsightsUseCase(
    private val weightRepository: WeightRepository,
    private val dailyHabitRepository: DailyHabitRepository,
) {
    operator fun invoke(): Flow<InsightsData> {
        return combine(
            weightRepository.getWeightProgress(),
            dailyHabitRepository.getAllDailyHabits(),
        ) { weights, habits ->
            val initialWeight = weights.firstOrNull()?.weight ?: 0f
            val currentWeight = weights.lastOrNull()?.weight ?: 0f

            val totalHabits = habits.size * 2
            val completedHabits = habits.count { it.didExercise } + habits.count { it.ateHealthy }
            val consistency = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

            InsightsData(
                weightEntries = weights,
                habitConsistency = consistency,
                totalWeightLost = initialWeight - currentWeight,
                currentWeight = currentWeight,
                initialWeight = initialWeight,
                exerciseDays = habits.count { it.didExercise },
                healthyEatingDays = habits.count { it.ateHealthy },
            )
        }
    }
}

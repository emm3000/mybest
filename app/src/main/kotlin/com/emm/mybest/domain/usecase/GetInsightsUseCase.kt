package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.InsightsData
import com.emm.mybest.domain.models.InsightsRecommendation
import com.emm.mybest.domain.repository.DailyHabitRepository
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val LOW_CONSISTENCY_THRESHOLD = 0.4f

class GetInsightsUseCase(
    private val weightRepository: WeightRepository,
    private val dailyHabitRepository: DailyHabitRepository,
    private val photoRepository: PhotoRepository,
) {
    operator fun invoke(): Flow<InsightsData> {
        return combine(
            weightRepository.getWeightProgress(),
            dailyHabitRepository.getAllDailyHabits(),
            photoRepository.getAllPhotos(),
        ) { weights, habits, photos ->
            val initialWeight = weights.firstOrNull()?.weight ?: 0f
            val currentWeight = weights.lastOrNull()?.weight ?: 0f

            val totalHabits = habits.size * 2
            val completedHabits = habits.count { it.didExercise } + habits.count { it.ateHealthy }
            val consistency = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f
            val recommendation = buildRecommendation(
                consistency = consistency,
                totalWeightLost = initialWeight - currentWeight,
                photoCount = photos.size,
                hasWeightTrend = weights.size >= 2,
            )

            InsightsData(
                weightEntries = weights,
                habitConsistency = consistency,
                totalWeightLost = initialWeight - currentWeight,
                currentWeight = currentWeight,
                initialWeight = initialWeight,
                exerciseDays = habits.count { it.didExercise },
                healthyEatingDays = habits.count { it.ateHealthy },
                photoCount = photos.size,
                recommendation = recommendation,
            )
        }
    }
}

private fun buildRecommendation(
    consistency: Float,
    totalWeightLost: Float,
    photoCount: Int,
    hasWeightTrend: Boolean,
): InsightsRecommendation {
    return when {
        consistency < LOW_CONSISTENCY_THRESHOLD -> InsightsRecommendation(
            title = "Refuerza la constancia",
            description = "Completa al menos 1 hábito diario esta semana para recuperar ritmo.",
            actionLabel = "Prioriza un hábito clave",
        )

        hasWeightTrend && totalWeightLost <= 0f -> InsightsRecommendation(
            title = "Ajusta tu plan semanal",
            description = "No hay mejora reciente de peso. Ajusta alimentación o entrenamiento 3 días esta semana.",
            actionLabel = "Define un ajuste concreto",
        )

        photoCount < 2 -> InsightsRecommendation(
            title = "Registra evidencia visual",
            description = "Añade al menos 2 fotos por semana para comparar cambios reales.",
            actionLabel = "Sube una foto hoy",
        )

        else -> InsightsRecommendation(
            title = "Mantén el ritmo",
            description = "Tu progreso es consistente. Conserva tu rutina y registra evidencia cada semana.",
            actionLabel = "Sostén la rutina actual",
        )
    }
}

package com.emm.mybest.domain.usecase

import com.emm.mybest.core.datetime.formatEsLongDate
import com.emm.mybest.domain.models.InsightsData
import com.emm.mybest.domain.models.InsightsRecommendation
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class GetInsightsUseCase(
    private val weightRepository: WeightRepository,
    private val photoRepository: PhotoRepository,
) {
    operator fun invoke(): Flow<InsightsData> {
        return combine(
            weightRepository.getWeightProgress(),
            photoRepository.getAllPhotos(),
        ) { weights, photos ->
            val initialWeight = weights.firstOrNull()?.weight ?: 0f
            val currentWeight = weights.lastOrNull()?.weight ?: 0f

            val recommendation = buildRecommendation(
                totalWeightLost = initialWeight - currentWeight,
                photoCount = photos.size,
                hasWeightTrend = weights.size >= 2,
            )
            val periodLabel = buildPeriodLabel(
                dates = buildList {
                    addAll(weights.map { it.date })
                    addAll(photos.map { it.date })
                },
            )

            InsightsData(
                weightEntries = weights,
                periodLabel = periodLabel,
                totalWeightLost = initialWeight - currentWeight,
                currentWeight = currentWeight,
                initialWeight = initialWeight,
                photoCount = photos.size,
                recommendation = recommendation,
            )
        }
    }
}

private fun buildPeriodLabel(dates: List<LocalDate>): String {
    val start = dates.minOrNull() ?: return "Sin periodo disponible aún."
    val end = dates.maxOrNull() ?: return "Sin periodo disponible aún."
    return if (start == end) {
        "Datos del ${start.formatEsLongDate()}"
    } else {
        "Datos del ${start.formatEsLongDate()} al ${end.formatEsLongDate()}"
    }
}

private fun buildRecommendation(
    totalWeightLost: Float,
    photoCount: Int,
    hasWeightTrend: Boolean,
): InsightsRecommendation {
    return when {
        hasWeightTrend && totalWeightLost <= 0f -> InsightsRecommendation(
            title = "Ajusta tu plan semanal",
            description = "No hay mejora reciente de peso. Ajusta alimentación o entrenamiento 3 días esta semana.",
            actionLabel = "Define un ajuste concreto",
            action = InsightsRecommendationAction.ADJUST_WEIGHT_PLAN,
        )

        photoCount < 2 -> InsightsRecommendation(
            title = "Registra evidencia visual",
            description = "Añade al menos 2 fotos por semana para comparar cambios reales.",
            actionLabel = "Sube una foto hoy",
            action = InsightsRecommendationAction.ADD_PROGRESS_PHOTO,
        )

        else -> InsightsRecommendation(
            title = "Mantén el ritmo",
            description = "Tu progreso es consistente. Conserva tu rutina y registra evidencia cada semana.",
            actionLabel = "Sostén la rutina actual",
            action = InsightsRecommendationAction.KEEP_ROUTINE,
        )
    }
}

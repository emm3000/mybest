package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.InsightsData
import com.emm.mybest.domain.models.InsightsRecommendation
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.domain.models.InsightsRecommendationKind
import com.emm.mybest.domain.models.PeriodLabel
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
            val period = buildPeriod(
                dates = buildList {
                    addAll(weights.map { it.date })
                    addAll(photos.map { it.date })
                },
            )

            InsightsData(
                weightEntries = weights,
                period = period,
                totalWeightLost = initialWeight - currentWeight,
                currentWeight = currentWeight,
                initialWeight = initialWeight,
                photoCount = photos.size,
                recommendation = recommendation,
            )
        }
    }
}

private fun buildPeriod(dates: List<LocalDate>): PeriodLabel {
    val start = dates.minOrNull() ?: return PeriodLabel.NoData
    val end = dates.maxOrNull() ?: return PeriodLabel.NoData
    return if (start == end) PeriodLabel.SingleDay(start) else PeriodLabel.Range(start, end)
}

private fun buildRecommendation(
    totalWeightLost: Float,
    photoCount: Int,
    hasWeightTrend: Boolean,
): InsightsRecommendation {
    return when {
        hasWeightTrend && totalWeightLost <= 0f -> InsightsRecommendation(
            kind = InsightsRecommendationKind.ADJUST_WEEKLY_PLAN,
            action = InsightsRecommendationAction.ADJUST_WEIGHT_PLAN,
        )

        photoCount < 2 -> InsightsRecommendation(
            kind = InsightsRecommendationKind.UPLOAD_PHOTO_TODAY,
            action = InsightsRecommendationAction.ADD_PROGRESS_PHOTO,
        )

        else -> InsightsRecommendation(
            kind = InsightsRecommendationKind.KEEP_ROUTINE,
            action = InsightsRecommendationAction.KEEP_ROUTINE,
        )
    }
}

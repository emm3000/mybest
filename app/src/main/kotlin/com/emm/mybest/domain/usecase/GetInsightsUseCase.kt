package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.InsightsData
import com.emm.mybest.domain.models.InsightsRecommendation
import com.emm.mybest.domain.models.InsightsRecommendationKind
import com.emm.mybest.domain.models.PeriodLabel
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class GetInsightsUseCase(
    private val weightRepository: WeightRepository,
    private val photoRepository: PhotoRepository,
) {
    operator fun invoke(today: LocalDate): Flow<InsightsData> {
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
                deltaWeightKg = computeDeltaKg(weights),
                deltaWeightPercent = computeDeltaPercent(weights),
                kgPerDayRate14d = computeRate14d(weights, today),
                daysSinceFirstWeight = computeDaysSinceFirstWeight(weights, today),
                troncoPhotoCount = photos.count { it.type == PhotoType.TRUNK },
                caraPhotoCount = photos.count { it.type == PhotoType.FACE },
            )
        }
    }

    private fun computeDeltaKg(weights: List<WeightEntry>): Float? {
        if (weights.size < 2) return null
        return weights.last().weight - weights.first().weight
    }

    private fun computeDeltaPercent(weights: List<WeightEntry>): Float? {
        if (weights.size < 2) return null
        val initial = weights.first().weight
        if (initial <= 0f) return null
        return (weights.last().weight - initial) / initial * PERCENT_FACTOR
    }

    private fun computeRate14d(weights: List<WeightEntry>, today: LocalDate): Float? {
        val baseline = weights.takeIf { it.size >= 2 }?.let { findBaseline(it, today) }
            ?: return null
        val last = weights.last()
        val daysBetween = last.date.toEpochDays() - baseline.date.toEpochDays()
        if (daysBetween <= 0) return null
        return (last.weight - baseline.weight) / daysBetween.toFloat()
    }

    private fun computeDaysSinceFirstWeight(weights: List<WeightEntry>, today: LocalDate): Int? {
        return weights.firstOrNull()?.date?.let { today.toEpochDays() - it.toEpochDays() }?.toInt()
    }

    private fun findBaseline(weights: List<WeightEntry>, today: LocalDate): WeightEntry? {
        val rangeStart = today.minus(DatePeriod(days = LOOKBACK_DAYS))
        val rangeEnd = today.minus(DatePeriod(days = LOOKBACK_DAYS - TOLERANCE_DAYS))
        val targetDays = rangeStart.toEpochDays()
        return weights
            .filter { it.date in rangeStart..rangeEnd }
            .minByOrNull { kotlin.math.abs(it.date.toEpochDays() - targetDays) }
    }

    private companion object {
        const val LOOKBACK_DAYS = 14
        const val TOLERANCE_DAYS = 7
        const val PERCENT_FACTOR = 100f
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
        )

        photoCount < 2 -> InsightsRecommendation(
            kind = InsightsRecommendationKind.UPLOAD_PHOTO_TODAY,
        )

        else -> InsightsRecommendation(
            kind = InsightsRecommendationKind.KEEP_ROUTINE,
        )
    }
}

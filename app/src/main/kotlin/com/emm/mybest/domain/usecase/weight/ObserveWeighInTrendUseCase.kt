package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.SetupRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class ObserveWeighInTrendUseCase(
    private val weightRepository: WeightRepository,
    private val setupRepository: SetupRepository,
) {
    operator fun invoke(): Flow<WeighInTrend> = observe { true }

    /**
     * Emits the trend of the weigh-ins dated on or before [upTo].
     */
    operator fun invoke(upTo: LocalDate): Flow<WeighInTrend> = observe { it.date <= upTo }

    private fun observe(isIncluded: (WeightEntry) -> Boolean): Flow<WeighInTrend> = combine(
        weightRepository.getWeightProgress(),
        setupRepository.observe(),
    ) { entries, setup -> trendOf(entries.filter(isIncluded), setup.targetWeightKg) }

    private fun trendOf(entries: List<WeightEntry>, targetKg: Float?): WeighInTrend {
        val latest = entries.lastOrNull()?.weight
        val previous = entries.getOrNull(entries.lastIndex - 1)?.weight
        return WeighInTrend(
            latestKg = latest,
            changeSincePreviousKg = if (latest != null && previous != null) latest - previous else null,
            remainingToTargetKg = remainingTo(targetKg, latest),
        )
    }

    private fun remainingTo(targetKg: Float?, latestKg: Float?): Float? {
        if (targetKg == null || latestKg == null) return null
        return (latestKg - targetKg).takeIf { it > 0f }
    }
}

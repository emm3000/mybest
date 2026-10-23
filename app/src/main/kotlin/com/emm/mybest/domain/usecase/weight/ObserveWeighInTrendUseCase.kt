package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.domain.repository.SetupRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveWeighInTrendUseCase(
    private val weightRepository: WeightRepository,
    private val setupRepository: SetupRepository,
) {
    operator fun invoke(): Flow<WeighInTrend> = combine(
        weightRepository.getWeightProgress(),
        setupRepository.observe(),
    ) { entries, setup ->
        val latest = entries.lastOrNull()?.weight
        val previous = entries.getOrNull(entries.lastIndex - 1)?.weight
        WeighInTrend(
            latestKg = latest,
            changeSincePreviousKg = if (latest != null && previous != null) latest - previous else null,
            remainingToTargetKg = remainingTo(setup.targetWeightKg, latest),
        )
    }

    private fun remainingTo(targetKg: Float?, latestKg: Float?): Float? {
        if (targetKg == null || latestKg == null) return null
        return (latestKg - targetKg).takeIf { it > 0f }
    }
}

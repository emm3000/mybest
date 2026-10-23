package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.money.averageOf
import com.emm.mybest.domain.models.OutingCostEstimate
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val OUTINGS_NEEDED_TO_AVERAGE = 3

class GetEstimatedOutingCostUseCase(
    private val setupRepository: SetupRepository,
    private val outingRepository: OutingRepository,
) {

    /**
     * Emits the estimated outing cost: the typical cost from setup until three outings have a check-in, then the
     * average of their total spent. The cost is null while neither is available.
     */
    operator fun invoke(): Flow<OutingCostEstimate> = combine(
        setupRepository.observe(),
        outingRepository.observeAll(),
    ) { setup, outings ->
        val spent = outings.mapNotNull { it.checkIn?.totalSpent }
        if (spent.size < OUTINGS_NEEDED_TO_AVERAGE) {
            OutingCostEstimate(cost = setup.typicalOutingCost, isAveraged = false)
        } else {
            OutingCostEstimate(cost = averageOf(spent), isAveraged = true)
        }
    }
}

package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.money.Money
import com.emm.mybest.core.money.averageOf
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal const val OUTINGS_NEEDED_TO_AVERAGE = 3

class GetEstimatedOutingCostUseCase(
    private val setupRepository: SetupRepository,
    private val outingRepository: OutingRepository,
) {

    /**
     * Emits the estimated outing cost: the typical cost from setup until three outings have a check-in, then the
     * average of their total spent. Null while neither is available.
     */
    operator fun invoke(): Flow<Money?> = combine(
        setupRepository.observe(),
        outingRepository.observeAll(),
    ) { setup, outings ->
        val spent = outings.mapNotNull { it.checkIn?.totalSpent }
        if (spent.size < OUTINGS_NEEDED_TO_AVERAGE) setup.typicalOutingCost else averageOf(spent)
    }
}

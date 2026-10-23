package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveIsEstimateAveragedUseCase(
    private val outingRepository: OutingRepository,
) {

    /**
     * Emits whether enough outings have a check-in for the estimated outing cost to be their average rather than the
     * typical cost declared in setup.
     */
    operator fun invoke(): Flow<Boolean> = outingRepository.observeAll().map { outings ->
        outings.count { it.checkIn != null } >= OUTINGS_NEEDED_TO_AVERAGE
    }
}

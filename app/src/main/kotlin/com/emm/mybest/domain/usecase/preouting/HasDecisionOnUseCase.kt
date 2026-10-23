package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

class HasDecisionOnUseCase(
    private val outingRepository: OutingRepository,
) {

    /**
     * Whether [appDay] already holds an outing or a won night.
     */
    suspend operator fun invoke(appDay: LocalDate): Boolean =
        outingRepository.observeRange(appDay, appDay).first().isNotEmpty() ||
            outingRepository.observeWonNights(appDay, appDay).first().isNotEmpty()
}

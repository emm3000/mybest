package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveHasPendingCheckInUseCase(
    private val outingRepository: OutingRepository,
) {

    /**
     * Emits whether any outing, of any app day, is still waiting for its check-in.
     */
    operator fun invoke(): Flow<Boolean> = outingRepository.observePending().map { it.isNotEmpty() }
}

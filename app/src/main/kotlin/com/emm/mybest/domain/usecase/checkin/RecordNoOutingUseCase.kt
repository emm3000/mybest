package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.datetime.LocalDate

class RecordNoOutingUseCase(
    private val outingRepository: OutingRepository,
) {

    /**
     * Records that the user did not go out on [eveningAppDay], so the question is not asked again for it.
     */
    suspend operator fun invoke(eveningAppDay: LocalDate) {
        outingRepository.recordNoOuting(eveningAppDay)
    }
}

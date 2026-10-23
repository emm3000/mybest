package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

class RecordUnplannedOutingUseCase(
    private val outingRepository: OutingRepository,
) {

    /**
     * Records an unplanned outing on [eveningAppDay], replacing the won night recorded on it if there is one. Nothing
     * changes when the evening already has an outing.
     */
    suspend operator fun invoke(eveningAppDay: LocalDate): RecordUnplannedOutingResult {
        if (outingRepository.observeRange(eveningAppDay, eveningAppDay).first().isNotEmpty()) {
            return RecordUnplannedOutingResult.AlreadyRecorded
        }
        outingRepository.recordUnplannedReplacingWonNight(eveningAppDay)
        return RecordUnplannedOutingResult.Recorded
    }
}

package com.emm.mybest.features.checkin.presentation

import com.emm.mybest.domain.usecase.checkin.ObserveCheckInTaskUseCase
import com.emm.mybest.domain.usecase.checkin.RecordNoOutingUseCase
import com.emm.mybest.domain.usecase.checkin.RecordUnplannedOutingUseCase
import com.emm.mybest.domain.usecase.checkin.SaveCheckInUseCase

data class CheckInUseCases(
    val observeTask: ObserveCheckInTaskUseCase,
    val saveCheckIn: SaveCheckInUseCase,
    val recordUnplannedOuting: RecordUnplannedOutingUseCase,
    val recordNoOuting: RecordNoOutingUseCase,
)

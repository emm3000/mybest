package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.Flow

class ObserveSetupUseCase(
    private val setupRepository: SetupRepository,
) {
    operator fun invoke(): Flow<Setup> = setupRepository.observe()
}

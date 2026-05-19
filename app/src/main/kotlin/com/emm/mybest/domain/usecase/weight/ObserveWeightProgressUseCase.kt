package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow

class ObserveWeightProgressUseCase(
    private val weightRepository: WeightRepository,
) {
    operator fun invoke(): Flow<List<WeightEntry>> = weightRepository.getWeightProgress()
}

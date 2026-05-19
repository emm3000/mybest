package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.repository.WeightRepository

class SaveWeightUseCase(
    private val weightRepository: WeightRepository,
) {
    suspend operator fun invoke(weight: Float, note: String?) {
        weightRepository.saveWeight(weight, note)
    }
}

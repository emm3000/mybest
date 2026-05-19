package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.datetime.LocalDate

class DeleteWeightByDateUseCase(
    private val weightRepository: WeightRepository,
) {
    suspend operator fun invoke(date: LocalDate) {
        weightRepository.deleteByDate(date)
    }
}

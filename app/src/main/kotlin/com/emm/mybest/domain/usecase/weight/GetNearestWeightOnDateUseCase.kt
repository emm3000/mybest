package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNearestWeightOnDateUseCase(
    private val weightRepository: WeightRepository,
) {
    operator fun invoke(): Flow<NearestWeightLookup> =
        weightRepository.getWeightProgress().map { entries ->
            NearestWeightLookup(entries.sortedBy { it.date })
        }
}

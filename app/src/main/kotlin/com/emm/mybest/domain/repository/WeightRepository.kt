package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun getWeightProgress(): Flow<List<WeightEntry>>
    suspend fun saveWeight(
        weight: Float,
        note: String?,
    )
}

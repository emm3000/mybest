package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface WeightRepository {
    fun getWeightProgress(): Flow<List<WeightEntry>>
    suspend fun saveWeight(
        weight: Float,
        note: String?,
    )
    suspend fun deleteByDate(date: LocalDate)
}

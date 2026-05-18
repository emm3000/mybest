package com.emm.mybest.data

import com.emm.mybest.core.datetime.currentDate
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class WeightRepositoryImpl(
    private val dao: DailyWeightDao,
) : WeightRepository {

    override fun getWeightProgress(): Flow<List<WeightEntry>> {
        return dao.observeAllOrdered().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveWeight(
        weight: Float,
        note: String?,
    ) {
        dao.upsert(
            DailyWeightEntity(
                date = currentDate(),
                weight = weight,
                note = note,
            ),
        )
    }

    override suspend fun deleteByDate(date: LocalDate) {
        dao.deleteByDate(date)
    }
}

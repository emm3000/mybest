package com.emm.mybest.data

import com.emm.mybest.data.entities.DailyCheckDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.repository.DailyCheckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class DailyCheckRepositoryImpl(
    private val dao: DailyCheckDao,
) : DailyCheckRepository {

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<DailyCheck>> =
        dao.observeRange(from, to).map { list -> list.map { it.toDomain() } }

    override suspend fun setDone(appDay: LocalDate, type: DailyCheckType, isDone: Boolean) {
        dao.upsert(DailyCheck(appDay = appDay, type = type, isDone = isDone).toEntity())
    }
}

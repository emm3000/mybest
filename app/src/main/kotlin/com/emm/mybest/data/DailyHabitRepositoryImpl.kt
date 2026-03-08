package com.emm.mybest.data

import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.repository.DailyHabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class DailyHabitRepositoryImpl(
    private val dao: DailyHabitDao
) : DailyHabitRepository {

    override fun getAllDailyHabits(): Flow<List<DailyHabitSummary>> {
        return dao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deleteByDate(date: LocalDate) {
        dao.deleteByDate(date)
    }
}

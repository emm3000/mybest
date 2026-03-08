package com.emm.mybest.data

import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.repository.DailyHabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DailyHabitRepositoryImpl(
    private val dao: DailyHabitDao
) : DailyHabitRepository {

    override fun getAllDailyHabits(): Flow<List<DailyHabitSummary>> {
        return dao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }
}

private fun DailyHabitEntity.toDomain() = DailyHabitSummary(
    date = date,
    ateHealthy = ateHealthy,
    didExercise = didExercise,
    notes = notes
)

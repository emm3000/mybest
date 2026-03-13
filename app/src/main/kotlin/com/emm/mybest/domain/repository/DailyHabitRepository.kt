package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.DailyHabitSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface DailyHabitRepository {
    fun getAllDailyHabits(): Flow<List<DailyHabitSummary>>

    suspend fun deleteByDate(date: LocalDate)
}

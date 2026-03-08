package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.DailyHabitSummary
import kotlinx.coroutines.flow.Flow

interface DailyHabitRepository {
    fun getAllDailyHabits(): Flow<List<DailyHabitSummary>>
}

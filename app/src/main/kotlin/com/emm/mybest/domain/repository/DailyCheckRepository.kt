package com.emm.mybest.domain.repository

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface DailyCheckRepository {
    /**
     * Emits the daily checks recorded from [from] to [to], both app days included.
     */
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<DailyCheck>>

    suspend fun setDone(appDay: LocalDate, type: DailyCheckType, isDone: Boolean)
}

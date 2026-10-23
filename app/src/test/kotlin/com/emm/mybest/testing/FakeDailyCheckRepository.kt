package com.emm.mybest.testing

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.repository.DailyCheckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class FakeDailyCheckRepository(
    initial: List<DailyCheck> = emptyList(),
    private val isRangeIgnored: Boolean = false,
) : DailyCheckRepository {

    private val checks = MutableStateFlow(initial)

    val writes = mutableListOf<DailyCheck>()

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<DailyCheck>> =
        checks.map { all -> if (isRangeIgnored) all else all.filter { it.appDay in from..to } }

    override suspend fun setDone(appDay: LocalDate, type: DailyCheckType, isDone: Boolean) {
        writes += DailyCheck(appDay, type, isDone)
        checks.value = checks.value.filterNot { it.appDay == appDay && it.type == type } +
            DailyCheck(appDay, type, isDone)
    }
}

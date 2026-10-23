package com.emm.mybest.domain.usecase.dailycheck

import com.emm.mybest.core.datetime.appWeekOf
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.repository.DailyCheckRepository
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class ObserveWeekProgressUseCase(
    private val dailyCheckRepository: DailyCheckRepository,
    private val setupRepository: SetupRepository,
) {

    /**
     * Emits the progress of the app week containing [appDay], only for daily checks that have a weekly target.
     */
    operator fun invoke(appDay: LocalDate): Flow<Map<DailyCheckType, WeekProgress>> {
        val week = appWeekOf(appDay)
        return combine(
            setupRepository.observe(),
            dailyCheckRepository.observeRange(week.start, week.endInclusive),
        ) { setup, checks ->
            setup.weeklyTargets.mapValues { (type, target) -> WeekProgress(countDone(checks, type, week), target) }
        }
    }

    private fun countDone(checks: List<DailyCheck>, type: DailyCheckType, week: ClosedRange<LocalDate>): Int =
        checks.count { it.type == type && it.isDone && it.appDay in week }
}

package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.core.datetime.appWeekOf
import com.emm.mybest.domain.models.WeekRelapses
import com.emm.mybest.domain.usecase.relapse.ObserveRelapsesUseCase
import com.emm.mybest.domain.usecase.relapse.ObserveReturnTimesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class ObserveWeekRelapsesUseCase(
    private val observeRelapses: ObserveRelapsesUseCase,
    private val observeReturnTimes: ObserveReturnTimesUseCase,
) {

    /**
     * Emits the relapses whose app day is in the app week containing [appDay], oldest first, and the return times of
     * every relapse up to the end of that week as the relapse and return rules define them, so a past week never
     * shows a later relapse. The latest return time belongs to the newest relapse of that week.
     */
    operator fun invoke(appDay: LocalDate): Flow<WeekRelapses> {
        val week = appWeekOf(appDay)
        return combine(observeRelapses(), observeReturnTimes()) { relapses, returnTimes ->
            val history = returnTimes.filter { it.relapseAppDay <= week.endInclusive }
            WeekRelapses(
                relapses = relapses.filter { it.outing.appDay in week },
                latestReturnTime = history.firstOrNull { it.relapseAppDay in week },
                returnTimeHistory = history,
            )
        }
    }
}

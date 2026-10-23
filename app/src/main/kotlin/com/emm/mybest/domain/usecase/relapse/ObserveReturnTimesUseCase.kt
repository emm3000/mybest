package com.emm.mybest.domain.usecase.relapse

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.ReturnTime
import com.emm.mybest.domain.repository.DailyCheckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

class ObserveReturnTimesUseCase(
    private val observeRelapses: ObserveRelapsesUseCase,
    private val dailyCheckRepository: DailyCheckRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits one return time per relapse, newest relapse first. The return is the first app day after the relapse, up
     * to the current app day, with diet done and no alcohol mark; an app day without an alcohol mark counts as no
     * alcohol. A relapse without such a day yet is open, with a null return and days.
     */
    operator fun invoke(): Flow<List<ReturnTime>> = appDayClock.observeToday().flatMapLatest { today ->
        observeRelapses().flatMapLatest { relapses ->
            returnTimesOf(relapses.map { it.outing.appDay }, today)
        }
    }

    private fun returnTimesOf(relapseDays: List<LocalDate>, today: LocalDate): Flow<List<ReturnTime>> {
        val earliest = relapseDays.minOrNull() ?: return flowOf(emptyList())
        return dailyCheckRepository.observeRange(earliest.plus(1, DateTimeUnit.DAY), today).map { checks ->
            val returnDays = qualifyingDays(checks)
            relapseDays.sortedDescending().map { returnTimeOf(it, returnDays) }
        }
    }

    private fun qualifyingDays(checks: List<DailyCheck>): List<LocalDate> {
        val dietDays = daysMarkedDone(checks, DailyCheckType.DIET)
        val drinkingDays = daysMarkedDone(checks, DailyCheckType.ALCOHOL)
        return (dietDays - drinkingDays).sorted()
    }

    private fun daysMarkedDone(checks: List<DailyCheck>, type: DailyCheckType): Set<LocalDate> =
        checks.filter { it.type == type && it.isDone }.mapTo(mutableSetOf()) { it.appDay }

    private fun returnTimeOf(relapseDay: LocalDate, qualifyingDays: List<LocalDate>): ReturnTime {
        val returnDay = qualifyingDays.firstOrNull { it > relapseDay }
        return ReturnTime(relapseDay, returnDay, returnDay?.let { relapseDay.daysUntil(it) })
    }
}

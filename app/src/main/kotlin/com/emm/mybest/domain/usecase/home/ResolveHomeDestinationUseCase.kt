package com.emm.mybest.domain.usecase.home

import com.emm.mybest.core.datetime.APP_DAY_START
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.EVENING_START
import com.emm.mybest.core.datetime.MORNING_AFTER_CUTOFF
import com.emm.mybest.domain.models.NightDecision
import com.emm.mybest.domain.models.Tonight
import com.emm.mybest.domain.usecase.checkin.CheckInTask
import com.emm.mybest.domain.usecase.checkin.ObserveCheckInTaskUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveTonightUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

private val WINDOW_STARTS = listOf(APP_DAY_START, MORNING_AFTER_CUTOFF, EVENING_START)

class ResolveHomeDestinationUseCase(
    private val observeCheckInTask: ObserveCheckInTaskUseCase,
    private val observeTonight: ObserveTonightUseCase,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits what Home should show, recomputed when the records change or the app day, the morning or the evening
     * begins. A morning with a check-in task opens the check-in; an evening with no decision on a Friday or Saturday
     * opens the pre-outing; a Sunday evening opens the weekly summary; anything else shows the daily checks.
     */
    operator fun invoke(): Flow<HomeDestination> = combine(
        appDayClock.observeLocalDateTime(WINDOW_STARTS),
        observeCheckInTask(),
        observeTonight(),
    ) { now, task, tonight -> destinationOf(now.time, task, tonight) }.distinctUntilChanged()

    private fun destinationOf(time: LocalTime, task: CheckInTask, tonight: Tonight): HomeDestination = when {
        isMorning(time) && task != CheckInTask.NoTask -> HomeDestination.CHECK_IN
        !isEvening(time) -> HomeDestination.DAILY_CHECKS
        tonight.canStayIn && tonight.decision == NightDecision.Undecided -> HomeDestination.PRE_OUTING
        tonight.appDay.dayOfWeek == DayOfWeek.SUNDAY -> HomeDestination.WEEKLY_SUMMARY
        else -> HomeDestination.DAILY_CHECKS
    }

    private fun isMorning(time: LocalTime): Boolean = time >= APP_DAY_START && time < MORNING_AFTER_CUTOFF

    private fun isEvening(time: LocalTime): Boolean = time >= EVENING_START || time < APP_DAY_START
}

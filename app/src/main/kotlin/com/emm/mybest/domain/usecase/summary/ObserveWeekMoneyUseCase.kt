package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.core.datetime.appWeekOf
import com.emm.mybest.domain.models.GoalProgress
import com.emm.mybest.domain.models.WeekDebt
import com.emm.mybest.domain.models.WeekMoney
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.models.estimatedTotal
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.usecase.goal.ObserveGoalProgressUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class ObserveWeekMoneyUseCase(
    private val outingRepository: OutingRepository,
    private val observeWeekDebt: ObserveWeekDebtUseCase,
    private val observeGoalProgress: ObserveGoalProgressUseCase,
) {

    /**
     * Emits the won nights of the app week containing [appDay] with their estimated total, the debt, and the goal
     * progress.
     */
    operator fun invoke(appDay: LocalDate): Flow<WeekMoney> {
        val week = appWeekOf(appDay)
        return combine(
            outingRepository.observeWonNights(week.start, week.endInclusive),
            observeWeekDebt(appDay),
            observeGoalProgress(),
            ::moneyOf,
        )
    }

    private fun moneyOf(wonNights: List<WonNight>, debt: WeekDebt, goal: GoalProgress?) =
        WeekMoney(wonNights, wonNights.estimatedTotal(), debt, goal)
}

package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.repository.OutingRepository

class PlanOutingUseCase(
    private val outingRepository: OutingRepository,
    private val hasDecisionOn: HasDecisionOnUseCase,
    private val appDayClock: AppDayClock,
) {

    /**
     * Records a planned outing with [limits] on the current app day. Nothing is recorded when the money limit is not
     * positive, the drinks limit is negative, or the app day already has an outing or a won night.
     */
    suspend operator fun invoke(limits: Limits): PlanOutingResult {
        limitsRejection(limits)?.let { return it }
        val today = appDayClock.today()
        if (hasDecisionOn(today)) return PlanOutingResult.DayAlreadyDecided
        outingRepository.recordPlanned(today, limits)
        return PlanOutingResult.Planned
    }

    private fun limitsRejection(limits: Limits): PlanOutingResult? = when {
        limits.money.cents <= 0 -> PlanOutingResult.NonPositiveMoney
        limits.drinks < 0 -> PlanOutingResult.NegativeDrinks
        else -> null
    }
}

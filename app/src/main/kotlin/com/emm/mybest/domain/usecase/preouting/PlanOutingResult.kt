package com.emm.mybest.domain.usecase.preouting

/**
 * What happened when the user tried to record a planned outing.
 */
sealed interface PlanOutingResult {
    data object Planned : PlanOutingResult
    data object NonPositiveMoney : PlanOutingResult
    data object NegativeDrinks : PlanOutingResult
    data object DayAlreadyDecided : PlanOutingResult
}

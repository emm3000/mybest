package com.emm.mybest.domain.usecase.goal

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.validation.ContributionValidationError

class AddGoalContributionUseCase(
    private val purchaseGoalRepository: PurchaseGoalRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Records [amount] as a real contribution on the current app day and returns null, or returns the validation error
     * and stores nothing when [amount] is missing or not above zero.
     */
    suspend operator fun invoke(amount: Money?): ContributionValidationError? {
        if (amount == null || amount.cents <= 0) return ContributionValidationError.NonPositiveAmount
        purchaseGoalRepository.addContribution(appDayClock.today(), amount)
        return null
    }
}

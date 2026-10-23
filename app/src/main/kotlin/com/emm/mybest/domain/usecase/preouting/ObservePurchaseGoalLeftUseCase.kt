package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObservePurchaseGoalLeftUseCase(
    private val purchaseGoalRepository: PurchaseGoalRepository,
) {

    /**
     * Emits the price of the purchase goal minus its real contributions, never below zero, or null without a goal.
     */
    operator fun invoke(): Flow<Money?> = combine(
        purchaseGoalRepository.observeGoal(),
        purchaseGoalRepository.observeContributions(),
    ) { goal, contributions ->
        goal?.let { maxOf(it.price - contributions.fold(Money.ZERO) { total, c -> total + c.amount }, Money.ZERO) }
    }
}

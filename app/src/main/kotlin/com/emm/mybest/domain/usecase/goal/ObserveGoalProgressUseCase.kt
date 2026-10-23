package com.emm.mybest.domain.usecase.goal

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.GoalProgress
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val FULL_PERCENT = 100

class ObserveGoalProgressUseCase(
    private val purchaseGoalRepository: PurchaseGoalRepository,
) {

    /**
     * Emits the progress of the purchase goal, or null without a goal. Progress is the sum of real contributions only;
     * estimated savings never count.
     */
    operator fun invoke(): Flow<GoalProgress?> = combine(
        purchaseGoalRepository.observeGoal(),
        purchaseGoalRepository.observeContributions(),
    ) { goal, contributions -> goal?.let { progressOf(it, contributions) } }

    private fun progressOf(goal: PurchaseGoal, contributions: List<GoalContribution>): GoalProgress {
        val contributed = contributions.fold(Money.ZERO) { total, contribution -> total + contribution.amount }
        return GoalProgress(
            goal = goal,
            contributed = contributed,
            left = maxOf(goal.price - contributed, Money.ZERO),
            percent = contributed.percentOf(goal.price)?.coerceIn(0, FULL_PERCENT) ?: 0,
            contributions = contributions.sortedWith(NEWEST_FIRST),
        )
    }

    private companion object {
        val NEWEST_FIRST = compareByDescending<GoalContribution> { it.appDay }.thenByDescending { it.id }
    }
}

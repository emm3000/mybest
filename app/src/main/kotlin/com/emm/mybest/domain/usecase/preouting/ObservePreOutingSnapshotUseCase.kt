package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.PreOutingSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObservePreOutingSnapshotUseCase(
    private val useCases: PreOutingUseCases,
) {

    /**
     * Emits every number shown before the decision to go out, recomputed from stored data on each change.
     */
    operator fun invoke(): Flow<PreOutingSnapshot> = combine(
        useCases.outingMonth(),
        useCases.nearestInstallment(),
        useCases.purchaseGoalLeft(),
        combine(useCases.estimatedOutingCost(), useCases.isEstimateAveraged(), ::Estimate),
        useCases.notSpentThisMonth(),
    ) { month, installment, goalLeft, estimate, notSpent ->
        PreOutingSnapshot(
            spentOnOutingsThisMonth = month.spent,
            outingsThisMonth = month.count,
            monthlyOutingLimit = month.limit,
            isNextOutingOverLimit = month.isNextOutingOverLimit,
            estimatedOutingCost = estimate.cost,
            isEstimateAveraged = estimate.isAveraged,
            nearestInstallment = installment,
            purchaseGoalLeft = goalLeft,
            notSpentThisMonth = notSpent,
        )
    }
}

private data class Estimate(val cost: Money?, val isAveraged: Boolean)

package com.emm.mybest.domain.usecase.preouting

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
        useCases.installmentInputs(),
        useCases.purchaseGoalLeft(),
        useCases.estimatedOutingCost(),
        useCases.notSpentThisMonth(),
    ) { month, inputs, goalLeft, estimate, notSpent ->
        PreOutingSnapshot(
            spentOnOutingsThisMonth = month.spent,
            outingsThisMonth = month.count,
            monthlyOutingLimit = month.limit,
            isNextOutingOverLimit = month.isNextOutingOverLimit,
            estimatedOutingCost = estimate.cost,
            isEstimateAveraged = estimate.isAveraged,
            hasDebts = inputs.hasDebts,
            nearestInstallment = inputs.nearestShare(estimate.cost),
            purchaseGoalLeft = goalLeft,
            notSpentThisMonth = notSpent,
        )
    }
}

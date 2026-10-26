package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.domain.models.InstallmentShare
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
        observeDebts(),
        useCases.purchaseGoalLeft(),
        useCases.estimatedOutingCost(),
        useCases.notSpentThisMonth(),
    ) { month, debts, goalLeft, estimate, notSpent ->
        PreOutingSnapshot(
            spentOnOutingsThisMonth = month.spent,
            outingsThisMonth = month.count,
            monthlyOutingLimit = month.limit,
            isNextOutingOverLimit = month.isNextOutingOverLimit,
            estimatedOutingCost = estimate.cost,
            isEstimateAveraged = estimate.isAveraged,
            hasDebts = debts.hasDebts,
            nearestInstallment = debts.nearestInstallment,
            purchaseGoalLeft = goalLeft,
            notSpentThisMonth = notSpent,
        )
    }

    private fun observeDebts(): Flow<DebtFacts> = combine(
        useCases.debtSummary(),
        useCases.nearestInstallment(),
    ) { summary, installment ->
        DebtFacts(hasDebts = summary.count > 0, nearestInstallment = installment)
    }
}

private data class DebtFacts(
    val hasDebts: Boolean,
    val nearestInstallment: InstallmentShare?,
)

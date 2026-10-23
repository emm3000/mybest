package com.emm.mybest.features.preouting.presentation

import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.PreOutingSnapshot

internal fun PreOutingSnapshot.toState(goalName: String?): PreOutingState = PreOutingState(
    isLoaded = true,
    spentOnOutingsLabel = spentOnOutingsThisMonth.formatPen(),
    estimatedOutingCostLabel = estimatedOutingCost?.formatPen(),
    isEstimateAveraged = isEstimateAveraged,
    debtName = nearestInstallment?.debtName.orEmpty(),
    paymentDay = nearestInstallment?.paymentDate?.day ?: 0,
    nightPercent = nearestInstallment?.nightPercent,
    goalName = goalName,
    goalLeftLabel = purchaseGoalLeft?.formatPen(),
    outingsThisMonth = outingsThisMonth,
    monthlyOutingLimit = monthlyOutingLimit,
    isNextOutingOverLimit = isNextOutingOverLimit,
    notSpentLabel = notSpentThisMonth.formatPen(),
    prompts = missingInputs(),
)

private fun PreOutingSnapshot.missingInputs(): Set<PreOutingPrompt> = buildSet {
    if (estimatedOutingCost == null) add(PreOutingPrompt.TYPICAL_COST)
    if (nearestInstallment == null) add(PreOutingPrompt.DEBTS)
    if (purchaseGoalLeft == null) add(PreOutingPrompt.PURCHASE_GOAL)
    if (monthlyOutingLimit == null) add(PreOutingPrompt.OUTING_LIMIT)
}

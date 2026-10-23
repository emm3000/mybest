package com.emm.mybest.features.preouting.presentation

import com.emm.mybest.core.datetime.MONTH_ABBR_ES
import com.emm.mybest.core.datetime.formatHhMm
import com.emm.mybest.core.datetime.shortEs
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.NightDecision
import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.Tonight
import kotlinx.datetime.LocalDate

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

internal fun PreOutingState.withDecision(tonight: Tonight, form: PreOutingForm): PreOutingState = copy(
    tonightLabel = tonight.appDay.toTonightLabel(),
    canStayIn = tonight.canStayIn,
    decision = tonight.decision.toState(),
    form = form,
    moneyLimitLabel = form.moneyLimit?.formatPen(),
    returnTimeLabel = form.returnTime?.formatHhMm(),
    canConfirmOuting = form.canConfirm,
)

private fun LocalDate.toTonightLabel(): String =
    "${dayOfWeek.shortEs()} · $day ${MONTH_ABBR_ES.getValue(month.ordinal + 1)}"

private fun NightDecision.toState(): PreOutingDecisionState = when (this) {
    NightDecision.Undecided -> PreOutingDecisionState.Undecided
    is NightDecision.WentOut -> PreOutingDecisionState.WentOut(
        moneyLabel = outing.limits?.money?.formatPen(),
        drinksLabel = outing.limits?.drinks?.toString(),
        returnTimeLabel = outing.limits?.returnTime?.formatHhMm(),
    )
    is NightDecision.StayedIn -> PreOutingDecisionState.StayedIn(wonNight.estimatedCost?.formatPen())
}

internal fun PreOutingPrompt.destination(): PreOutingEffect = when (this) {
    PreOutingPrompt.TYPICAL_COST, PreOutingPrompt.OUTING_LIMIT -> PreOutingEffect.OpenSettings
    PreOutingPrompt.DEBTS -> PreOutingEffect.OpenDebts
    PreOutingPrompt.PURCHASE_GOAL -> PreOutingEffect.OpenPurchaseGoal
}

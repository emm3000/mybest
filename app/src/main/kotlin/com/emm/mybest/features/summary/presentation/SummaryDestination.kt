package com.emm.mybest.features.summary.presentation

enum class SummaryDestination {
    SETTINGS,
    ADD_WEIGHT,
    DEBTS,
    PURCHASE_GOAL,
}

internal fun SummaryDestination.toEffect(): SummaryEffect = when (this) {
    SummaryDestination.SETTINGS -> SummaryEffect.OpenSettings
    SummaryDestination.ADD_WEIGHT -> SummaryEffect.OpenAddWeight
    SummaryDestination.DEBTS -> SummaryEffect.OpenDebts
    SummaryDestination.PURCHASE_GOAL -> SummaryEffect.OpenPurchaseGoal
}

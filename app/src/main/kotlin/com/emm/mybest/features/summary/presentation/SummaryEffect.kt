package com.emm.mybest.features.summary.presentation

sealed interface SummaryEffect {
    data object OpenSettings : SummaryEffect
    data object OpenAddWeight : SummaryEffect
    data object OpenDebts : SummaryEffect
    data object OpenPurchaseGoal : SummaryEffect
}

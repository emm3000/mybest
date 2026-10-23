package com.emm.mybest.core.navigation

import com.emm.mybest.features.summary.presentation.SummaryCallbacks

internal fun summaryCallbacks(navigator: Navigator): SummaryCallbacks = SummaryCallbacks(
    onOpenSettings = { navigator.navigate(Screen.Settings) },
    onOpenAddWeight = { navigator.navigate(Screen.AddWeight) },
    onOpenDebts = { navigator.navigate(Screen.Debts) },
    onOpenPurchaseGoal = { navigator.navigate(Screen.PurchaseGoal) },
)

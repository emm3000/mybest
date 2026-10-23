package com.emm.mybest.core.navigation

import com.emm.mybest.features.preouting.presentation.PreOutingCallbacks

internal fun preOutingCallbacks(navigator: Navigator): PreOutingCallbacks = PreOutingCallbacks(
    onOpenSettings = { navigator.navigate(Screen.Settings) },
    onOpenDebts = { navigator.navigate(Screen.Debts) },
    onOpenPurchaseGoal = { navigator.navigate(Screen.PurchaseGoal) },
)

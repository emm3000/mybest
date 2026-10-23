package com.emm.mybest.features.preouting.presentation

data class PreOutingCallbacks(
    val onOpenSettings: () -> Unit,
    val onOpenDebts: () -> Unit,
    val onOpenPurchaseGoal: () -> Unit,
)

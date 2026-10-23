package com.emm.mybest.features.summary.presentation

data class SummaryCallbacks(
    val onOpenSettings: () -> Unit,
    val onOpenAddWeight: () -> Unit,
    val onOpenDebts: () -> Unit,
    val onOpenPurchaseGoal: () -> Unit,
)

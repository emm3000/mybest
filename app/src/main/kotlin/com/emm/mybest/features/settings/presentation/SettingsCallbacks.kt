package com.emm.mybest.features.settings.presentation

import androidx.compose.runtime.Composable

data class SettingsCallbacks(
    val onDebtsClick: () -> Unit,
    val onPurchaseGoalClick: () -> Unit,
    val bottomBar: @Composable () -> Unit = {},
)

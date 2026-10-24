package com.emm.mybest.features.settings.presentation

import androidx.compose.material3.SnackbarHostState

data class SettingsSetupHost(
    val snackbarHostState: SnackbarHostState,
    val onDebtsClick: () -> Unit,
    val onPurchaseGoalClick: () -> Unit,
)

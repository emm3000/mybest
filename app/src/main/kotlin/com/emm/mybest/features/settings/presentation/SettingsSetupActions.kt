package com.emm.mybest.features.settings.presentation

internal data class SettingsSetupActions(
    val onFieldOpen: (SettingsField) -> Unit,
    val onReminderIntent: (SettingsIntent.ReminderIntent) -> Unit,
    val onDebtsClick: () -> Unit,
    val onPurchaseGoalClick: () -> Unit,
)

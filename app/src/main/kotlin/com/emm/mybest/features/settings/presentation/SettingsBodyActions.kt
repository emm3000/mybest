package com.emm.mybest.features.settings.presentation

internal data class SettingsBodyActions(
    val onReminderIntent: (SettingsIntent.ReminderIntent) -> Unit,
    val onFieldOpen: (SettingsField) -> Unit,
    val onDebtsClick: () -> Unit,
    val onPurchaseGoalClick: () -> Unit,
    val onCaptureClick: () -> Unit,
    val onExportClick: () -> Unit,
    val onImportClick: () -> Unit,
    val onReplayOnboardingClick: () -> Unit,
)

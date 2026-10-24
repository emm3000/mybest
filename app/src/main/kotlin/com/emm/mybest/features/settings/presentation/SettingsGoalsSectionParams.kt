package com.emm.mybest.features.settings.presentation

internal data class SettingsGoalsSectionParams(
    val rows: List<SettingsGoalRow>,
    val onFieldOpen: (SettingsField) -> Unit,
)

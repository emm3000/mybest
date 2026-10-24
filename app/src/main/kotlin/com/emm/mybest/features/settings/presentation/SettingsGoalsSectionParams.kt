package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.Setup

internal data class SettingsGoalsSectionParams(
    val setup: Setup,
    val onFieldOpen: (SettingsField) -> Unit,
    val fields: List<SettingsField> = SettingsField.entries,
)

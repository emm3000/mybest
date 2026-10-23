package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.Setup

data class SettingsGoals(
    val setup: Setup = Setup(),
    val editor: SettingsEditorState? = null,
)

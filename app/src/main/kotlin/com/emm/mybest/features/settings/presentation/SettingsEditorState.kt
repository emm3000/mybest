package com.emm.mybest.features.settings.presentation

data class SettingsEditorState(
    val field: SettingsField,
    val input: String,
    val error: SettingsFieldError? = null,
)

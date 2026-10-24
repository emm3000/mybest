package com.emm.mybest.features.settings.presentation

import androidx.compose.runtime.Composable

@Composable
internal fun SettingsEditors(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    state.reminderPicker?.let { field ->
        SettingsReminderPicker(field = field, setup = state.goals.setup, onIntent = onIntent)
    }
    state.goals.editor?.let { editor ->
        SettingsFieldEditor(
            editor = editor,
            actions = SettingsFieldEditorActions(
                onInputChange = { onIntent(SettingsIntent.OnFieldInputChange(it)) },
                onSave = { onIntent(SettingsIntent.OnFieldSave) },
                onClear = { onIntent(SettingsIntent.OnFieldClear) },
                onDismiss = { onIntent(SettingsIntent.OnFieldDismiss) },
            ),
        )
    }
}

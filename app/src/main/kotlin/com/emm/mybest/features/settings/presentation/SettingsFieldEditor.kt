package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.components.atelier.MicroLabel

private val EDITOR_GUT = 28.dp
private val EDITOR_GAP = 16.dp

internal data class SettingsFieldEditorActions(
    val onInputChange: (String) -> Unit,
    val onSave: () -> Unit,
    val onClear: () -> Unit,
    val onDismiss: () -> Unit,
)

@Composable
internal fun SettingsFieldEditor(
    editor: SettingsEditorState,
    actions: SettingsFieldEditorActions,
    modifier: Modifier = Modifier,
) {
    HBottomSheet(onDismissRequest = actions.onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = EDITOR_GUT, vertical = EDITOR_GAP),
            verticalArrangement = Arrangement.spacedBy(EDITOR_GAP),
        ) {
            MicroLabel(text = stringResource(editor.field.labelRes))
            HInput(
                value = editor.input,
                onValueChange = actions.onInputChange,
                errorMessage = editor.error?.let { stringResource(it.messageRes) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            EditorButtons(actions = actions)
        }
    }
}

@Composable
private fun EditorButtons(actions: SettingsFieldEditorActions) {
    Row(horizontalArrangement = Arrangement.spacedBy(EDITOR_GAP)) {
        HButton(onClick = actions.onSave) { Text(stringResource(R.string.settings_goal_save)) }
        HButton(onClick = actions.onClear, variant = ButtonVariant.Outline) {
            Text(stringResource(R.string.settings_goal_clear))
        }
    }
}

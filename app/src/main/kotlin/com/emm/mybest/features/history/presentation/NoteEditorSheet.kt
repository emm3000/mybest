package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.validation.NoteValidationError
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierBackground
import com.emm.mybest.ui.theme.AtelierTheme

private val SHEET_GUTTER = 28.dp
private val SHEET_GAP = 16.dp
private const val NOTE_MIN_LINES = 3
private const val NOTE_MAX_LINES = 8

@Composable
internal fun NoteEditorSheet(
    editor: NoteEditorState,
    onIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HBottomSheet(
        onDismissRequest = { onIntent(HistoryIntent.OnNoteDismiss) },
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AtelierBackground,
    ) {
        NoteEditorContent(editor = editor, onIntent = onIntent)
    }
}

@Composable
private fun NoteEditorContent(
    editor: NoteEditorState,
    onIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = SHEET_GUTTER, vertical = SHEET_GAP),
        verticalArrangement = Arrangement.spacedBy(SHEET_GAP),
    ) {
        MicroLabel(text = stringResource(editor.titleRes()))
        HInput(
            value = editor.text,
            onValueChange = { onIntent(HistoryIntent.OnNoteTextChange(it)) },
            label = stringResource(R.string.logbook_note_field),
            supportingText = stringResource(R.string.logbook_note_counter, editor.textLength, editor.maxLength),
            errorMessage = editor.error?.let { stringResource(it.messageRes(), editor.maxLength) },
            singleLine = false,
            minLines = NOTE_MIN_LINES,
            maxLines = NOTE_MAX_LINES,
        )
        NoteEditorButtons(isEditing = editor.isEditing, onIntent = onIntent)
    }
}

@Composable
private fun NoteEditorButtons(isEditing: Boolean, onIntent: (HistoryIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SHEET_GAP)) {
        Row(horizontalArrangement = Arrangement.spacedBy(SHEET_GAP)) {
            HButton(
                text = stringResource(R.string.logbook_note_save),
                onClick = { onIntent(HistoryIntent.OnNoteSave) },
            )
            HButton(
                text = stringResource(R.string.logbook_note_cancel),
                onClick = { onIntent(HistoryIntent.OnNoteDismiss) },
                variant = ButtonVariant.Outline,
            )
        }
        if (isEditing) {
            HButton(
                text = stringResource(R.string.logbook_note_delete),
                onClick = { onIntent(HistoryIntent.OnNoteDeleteClick) },
                variant = ButtonVariant.Destructive,
            )
        }
    }
}

private fun NoteEditorState.titleRes(): Int =
    if (isEditing) R.string.logbook_note_edit_title else R.string.logbook_note_new_title

private fun NoteValidationError.messageRes(): Int = when (this) {
    NoteValidationError.Blank -> R.string.logbook_note_error_blank
    NoteValidationError.TooLong -> R.string.logbook_note_error_too_long
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun NoteEditorContentPreview() {
    AtelierTheme {
        NoteEditorContent(
            editor = NoteEditorState(noteId = 3, text = "Volví a las 2", error = NoteValidationError.Blank),
            onIntent = {},
        )
    }
}

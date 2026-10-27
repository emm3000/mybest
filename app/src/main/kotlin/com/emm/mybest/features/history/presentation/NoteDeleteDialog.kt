package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R
import com.emm.mybest.ui.components.HAlertDialog

@Composable
internal fun NoteDeleteDialog(onIntent: (HistoryIntent) -> Unit, modifier: Modifier = Modifier) {
    HAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.bitacora_note_delete_title),
        description = stringResource(R.string.bitacora_note_delete_message),
        confirmText = stringResource(R.string.bitacora_note_delete_confirm),
        cancelText = stringResource(R.string.bitacora_note_cancel),
        onConfirm = { onIntent(HistoryIntent.OnNoteDeleteConfirm) },
        onDismiss = { onIntent(HistoryIntent.OnNoteDeleteDismiss) },
        isDangerous = true,
    )
}

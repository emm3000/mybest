package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Immutable
import com.emm.mybest.domain.validation.NOTE_MAX_LENGTH
import com.emm.mybest.domain.validation.NoteValidationError

@Immutable
data class NoteEditorState(
    val noteId: Long? = null,
    val text: String = "",
    val error: NoteValidationError? = null,
    val isConfirmingDelete: Boolean = false,
) {
    val isEditing: Boolean get() = noteId != null
    val textLength: Int get() = text.length
    val maxLength: Int get() = NOTE_MAX_LENGTH
}

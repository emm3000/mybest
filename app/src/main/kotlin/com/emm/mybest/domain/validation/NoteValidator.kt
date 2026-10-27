package com.emm.mybest.domain.validation

/**
 * The most characters (`String.length`) a note may hold once trimmed.
 */
const val NOTE_MAX_LENGTH = 500

/**
 * Checks the text of a note after trimming it at both ends. A null result means the note can be saved.
 */
object NoteValidator {

    fun validate(text: String): NoteValidationError? {
        val trimmed = text.trim()
        return when {
            trimmed.isEmpty() -> NoteValidationError.Blank
            trimmed.length > NOTE_MAX_LENGTH -> NoteValidationError.TooLong
            else -> null
        }
    }
}

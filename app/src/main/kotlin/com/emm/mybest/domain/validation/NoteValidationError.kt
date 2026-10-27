package com.emm.mybest.domain.validation

sealed interface NoteValidationError {
    data object Blank : NoteValidationError
    data object TooLong : NoteValidationError
}

package com.emm.mybest.domain.usecase.note

import com.emm.mybest.domain.repository.NoteRepository
import com.emm.mybest.domain.validation.NoteValidationError
import com.emm.mybest.domain.validation.NoteValidator

class UpdateNoteUseCase(
    private val noteRepository: NoteRepository,
) {

    /**
     * Replaces the text of the note with [id] by the trimmed [text] and returns null, or returns the validation
     * error and changes nothing. The app day and creation instant stay.
     */
    suspend operator fun invoke(id: Long, text: String): NoteValidationError? {
        NoteValidator.validate(text)?.let { return it }
        noteRepository.updateText(id, text.trim())
        return null
    }
}

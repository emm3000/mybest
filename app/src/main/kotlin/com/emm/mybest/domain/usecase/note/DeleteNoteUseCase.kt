package com.emm.mybest.domain.usecase.note

import com.emm.mybest.domain.repository.NoteRepository

class DeleteNoteUseCase(
    private val noteRepository: NoteRepository,
) {

    /**
     * Deletes the note with [id]. An unknown id changes nothing.
     */
    suspend operator fun invoke(id: Long) {
        noteRepository.delete(id)
    }
}

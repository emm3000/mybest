package com.emm.mybest.domain.usecase.note

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.repository.NoteRepository
import com.emm.mybest.domain.validation.NoteValidationError
import com.emm.mybest.domain.validation.NoteValidator

class AddNoteUseCase(
    private val noteRepository: NoteRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Stores the trimmed [text] as a note stamped now, on the app day of now, and returns null, or returns the
     * validation error and stores nothing.
     */
    suspend operator fun invoke(text: String): NoteValidationError? {
        NoteValidator.validate(text)?.let { return it }
        val now = appDayClock.now()
        noteRepository.add(appDayClock.appDayOf(now), now, text.trim())
        return null
    }
}

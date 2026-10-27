package com.emm.mybest.domain.usecase.note

import com.emm.mybest.domain.models.Note
import com.emm.mybest.domain.validation.NoteValidationError
import com.emm.mybest.testing.FakeNoteRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

private val ORIGINAL = Note(
    id = 4,
    appDay = LocalDate(2026, 11, 6),
    createdAt = Instant.fromEpochMilliseconds(1_794_022_200_000),
    text = "Volví a las 2",
)

class UpdateNoteUseCaseTest {

    private val repository = FakeNoteRepository(listOf(ORIGINAL))
    private val updateNote = UpdateNoteUseCase(repository)

    @Test
    fun `a new text is trimmed and keeps the app day and creation instant`() = runTest {
        val error = updateNote(ORIGINAL.id, " Nuevo texto ")

        assertNull(error)
        assertEquals(listOf(ORIGINAL.copy(text = "Nuevo texto")), repository.stored)
    }

    @Test
    fun `a blank text is rejected and leaves the note unchanged`() = runTest {
        val error = updateNote(ORIGINAL.id, " \n ")

        assertEquals(NoteValidationError.Blank, error)
        assertEquals(listOf(ORIGINAL), repository.stored)
    }

    @Test
    fun `a text of 501 characters is rejected and leaves the note unchanged`() = runTest {
        val error = updateNote(ORIGINAL.id, "a".repeat(501))

        assertEquals(NoteValidationError.TooLong, error)
        assertEquals(listOf(ORIGINAL), repository.stored)
    }
}

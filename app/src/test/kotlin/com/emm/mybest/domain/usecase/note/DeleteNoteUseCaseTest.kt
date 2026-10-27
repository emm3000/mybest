package com.emm.mybest.domain.usecase.note

import com.emm.mybest.domain.models.Note
import com.emm.mybest.testing.FakeNoteRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

private val FRIDAY_NOTE = Note(1, LocalDate(2026, 11, 6), Instant.fromEpochMilliseconds(1_794_022_200_000), "Viernes")
private val SATURDAY_NOTE = Note(2, LocalDate(2026, 11, 7), Instant.fromEpochMilliseconds(1_794_108_600_000), "Sábado")

class DeleteNoteUseCaseTest {

    private val repository = FakeNoteRepository(listOf(FRIDAY_NOTE, SATURDAY_NOTE))
    private val deleteNote = DeleteNoteUseCase(repository)

    @Test
    fun `the deleted note is gone and the others stay`() = runTest {
        deleteNote(FRIDAY_NOTE.id)

        assertEquals(listOf(SATURDAY_NOTE), repository.stored)
    }

    @Test
    fun `deleting an unknown id leaves every note`() = runTest {
        deleteNote(99)

        assertEquals(listOf(FRIDAY_NOTE, SATURDAY_NOTE), repository.stored)
    }
}

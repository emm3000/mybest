package com.emm.mybest.domain.usecase.note

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.Note
import com.emm.mybest.domain.validation.NoteValidationError
import com.emm.mybest.testing.FakeNoteRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

private val LIMA = TimeZone.of("America/Lima")
private val SATURDAY_02_30: Instant = LocalDateTime(2026, 11, 7, 2, 30).toInstant(LIMA)
private val SATURDAY_05_00: Instant = LocalDateTime(2026, 11, 7, 5, 0).toInstant(LIMA)

class AddNoteUseCaseTest {

    private val repository = FakeNoteRepository()

    private fun useCaseAt(instant: Instant) = AddNoteUseCase(
        noteRepository = repository,
        appDayClock = AppDayClock(FixedClock(instant), LIMA),
    )

    @Test
    fun `a note written before 05_00 is trimmed and belongs to the previous app day`() = runTest {
        val error = useCaseAt(SATURDAY_02_30)("  Volví a las 2  ")

        assertNull(error)
        assertEquals(listOf(Note(1, LocalDate(2026, 11, 6), SATURDAY_02_30, "Volví a las 2")), repository.stored)
    }

    @Test
    fun `a note written at 05_00 belongs to that app day`() = runTest {
        useCaseAt(SATURDAY_05_00)("Desayuno")

        assertEquals(LocalDate(2026, 11, 7), repository.stored.single().appDay)
    }

    @Test
    fun `a blank text is rejected and stores nothing`() = runTest {
        val error = useCaseAt(SATURDAY_05_00)("   ")

        assertEquals(NoteValidationError.Blank, error)
        assertEquals(emptyList<Note>(), repository.stored)
    }

    @Test
    fun `a text of 501 characters is rejected and stores nothing`() = runTest {
        val error = useCaseAt(SATURDAY_05_00)("a".repeat(501))

        assertEquals(NoteValidationError.TooLong, error)
        assertEquals(emptyList<Note>(), repository.stored)
    }
}

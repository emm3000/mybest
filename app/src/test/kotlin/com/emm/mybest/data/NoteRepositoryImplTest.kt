package com.emm.mybest.data

import com.emm.mybest.data.entities.NoteDao
import com.emm.mybest.data.entities.NoteEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

private val FRIDAY = LocalDate(2026, 11, 6)
private val SATURDAY = LocalDate(2026, 11, 7)
private val CREATED_AT = Instant.fromEpochMilliseconds(1_794_022_200_000)

class NoteRepositoryImplTest {

    private val noteDao = mockk<NoteDao>(relaxUnitFun = true)
    private val repository = NoteRepositoryImpl(noteDao)

    @Test
    fun `add inserts an entity stamped with the creation instant in epoch milliseconds`() = runTest {
        coEvery { noteDao.insert(any()) } returns 9

        val id = repository.add(FRIDAY, CREATED_AT, "Volví a las 2")

        assertEquals(9L, id)
        coVerify {
            noteDao.insert(NoteEntity(appDay = FRIDAY, createdAtEpochMs = 1_794_022_200_000, text = "Volví a las 2"))
        }
    }

    @Test
    fun `observeRange maps the rows in the order the dao returns them`() = runTest {
        every { noteDao.observeRange(FRIDAY, SATURDAY) } returns flowOf(
            listOf(
                NoteEntity(id = 2, appDay = SATURDAY, createdAtEpochMs = 1_794_108_600_000, text = "Sábado"),
                NoteEntity(id = 1, appDay = FRIDAY, createdAtEpochMs = 1_794_022_200_000, text = "Viernes"),
            ),
        )

        val notes = repository.observeRange(FRIDAY, SATURDAY).first()

        assertEquals(listOf(2L, 1L), notes.map { it.id })
        assertEquals(CREATED_AT, notes.last().createdAt)
    }

    @Test
    fun `updateText stores the new text`() = runTest {
        coEvery { noteDao.updateText(4, "Nuevo texto") } returns 1

        repository.updateText(4, "Nuevo texto")

        coVerify { noteDao.updateText(4, "Nuevo texto") }
    }

    @Test
    fun `updateText throws when no note has the id`() = runTest {
        coEvery { noteDao.updateText(any(), any()) } returns 0

        val failure = runCatching { repository.updateText(99, "x") }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
    }

    @Test
    fun `delete removes the note with the id`() = runTest {
        repository.delete(4)

        coVerify { noteDao.delete(4) }
    }
}

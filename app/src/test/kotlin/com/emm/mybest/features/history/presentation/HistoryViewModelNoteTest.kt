package com.emm.mybest.features.history.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.Note
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.GetLogbookUseCase
import com.emm.mybest.domain.usecase.history.HistoryResult
import com.emm.mybest.domain.usecase.history.LogbookDay
import com.emm.mybest.domain.usecase.history.LogbookEntry
import com.emm.mybest.domain.usecase.note.AddNoteUseCase
import com.emm.mybest.domain.usecase.note.DeleteNoteUseCase
import com.emm.mybest.domain.usecase.note.UpdateNoteUseCase
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import com.emm.mybest.domain.validation.NoteValidationError
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

private val LIMA = TimeZone.of("America/Lima")
private val MAY_FIFTEENTH = LocalDate(2026, 5, 15)
private val CLOCK = AppDayClock(FixedClock(LocalDateTime(2026, 5, 15, 12, 0).toInstant(LIMA)), LIMA)
private val STORED_NOTE = Note(5, MAY_FIFTEENTH, Instant.fromEpochSeconds(0), "Volví a las 2")
private val EMPTY_EDITOR = NoteEditorState(noteId = null, text = "", error = null, isConfirmingDelete = false)

class HistoryViewModelNoteTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val addNote = mockk<AddNoteUseCase>()
    private val updateNote = mockk<UpdateNoteUseCase>()
    private val deleteNote = mockk<DeleteNoteUseCase>(relaxed = true)

    private fun buildViewModel(): HistoryViewModel {
        val getHistory = mockk<GetHistoryUseCase>()
        every { getHistory(any()) } returns flowOf(HistoryResult(emptyMap(), 0, 0, 0, 0))
        val getLogbook = mockk<GetLogbookUseCase>()
        val day = LogbookDay(MAY_FIFTEENTH, listOf(LogbookEntry.NoteEntry(STORED_NOTE, LocalTime(2, 30))))
        every { getLogbook(any()) } returns flowOf(listOf(day))
        coEvery { addNote(any()) } returns null
        coEvery { updateNote(any(), any()) } returns null
        return HistoryViewModel(
            getHistory,
            getLogbook,
            mockk<DeleteWeightByDateUseCase>(relaxed = true),
            mockk<DeletePhotoUseCase>(relaxed = true),
            HistoryNoteUseCases(addNote, updateNote, deleteNote),
            CLOCK,
        )
    }

    private fun TestScope.send(viewModel: HistoryViewModel, vararg intents: HistoryIntent): HistoryState {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        intents.forEach { viewModel.onIntent(it) }
        advanceUntilIdle()
        return viewModel.state.value
    }

    @Test
    fun `canWriteNote is true on the current month and false on a previous one`() = runTest {
        val viewModel = buildViewModel()

        assertTrue(send(viewModel).canWriteNote)
        assertFalse(send(viewModel, HistoryIntent.OnMonthChange(YearMonthValue(2026, 4))).canWriteNote)
    }

    @Test
    fun `OnWriteNoteClick opens an empty editor`() = runTest {
        val state = send(buildViewModel(), HistoryIntent.OnWriteNoteClick)

        assertEquals(EMPTY_EDITOR, state.noteEditor)
    }

    @Test
    fun `a blank save keeps the editor with the error until the next text change`() = runTest {
        val viewModel = buildViewModel()
        coEvery { addNote(any()) } returns NoteValidationError.Blank

        val rejected = send(viewModel, HistoryIntent.OnWriteNoteClick, HistoryIntent.OnNoteSave)
        assertEquals(NoteValidationError.Blank, rejected.noteEditor?.error)

        val typed = send(viewModel, HistoryIntent.OnNoteTextChange("x"))
        assertEquals(EMPTY_EDITOR.copy(text = "x"), typed.noteEditor)
    }

    @Test
    fun `a valid save adds the typed text once and closes the editor`() = runTest {
        val viewModel = buildViewModel()

        val state = send(
            viewModel,
            HistoryIntent.OnWriteNoteClick,
            HistoryIntent.OnNoteTextChange("Volví a las 2"),
            HistoryIntent.OnNoteSave,
        )

        coVerify(exactly = 1) { addNote("Volví a las 2") }
        assertNull(state.noteEditor)
    }

    @Test
    fun `saving an opened note updates it and never adds`() = runTest {
        val viewModel = buildViewModel()

        val opened = send(viewModel, HistoryIntent.OnNoteClick(STORED_NOTE.id))
        assertEquals(NoteEditorState(STORED_NOTE.id, STORED_NOTE.text, null, false), opened.noteEditor)

        val saved = send(viewModel, HistoryIntent.OnNoteTextChange("Volví a las 3"), HistoryIntent.OnNoteSave)

        coVerify(exactly = 1) { updateNote(STORED_NOTE.id, "Volví a las 3") }
        coVerify(exactly = 0) { addNote(any()) }
        assertNull(saved.noteEditor)
    }

    @Test
    fun `delete asks first, can be dismissed and then deletes on confirm`() = runTest {
        val viewModel = buildViewModel()

        val asking = send(viewModel, HistoryIntent.OnNoteClick(STORED_NOTE.id), HistoryIntent.OnNoteDeleteClick)
        assertTrue(asking.noteEditor?.isConfirmingDelete == true)

        val dismissed = send(viewModel, HistoryIntent.OnNoteDeleteDismiss)
        assertEquals(NoteEditorState(STORED_NOTE.id, STORED_NOTE.text, null, false), dismissed.noteEditor)

        val confirmed = send(viewModel, HistoryIntent.OnNoteDeleteClick, HistoryIntent.OnNoteDeleteConfirm)

        coVerify(exactly = 1) { deleteNote(STORED_NOTE.id) }
        assertNull(confirmed.noteEditor)
    }

    @Test
    fun `a failing add emits one effect and keeps the editor with its text`() = runTest {
        val viewModel = buildViewModel()
        coEvery { addNote(any()) } throws IllegalStateException("db")

        viewModel.effect.test {
            val state = send(
                viewModel,
                HistoryIntent.OnWriteNoteClick,
                HistoryIntent.OnNoteTextChange("texto"),
                HistoryIntent.OnNoteSave,
            )

            assertEquals(HistoryEffect.NoteActionFailed, awaitItem())
            assertEquals(EMPTY_EDITOR.copy(text = "texto"), state.noteEditor)
        }
    }

    @Test
    fun `OnNoteDismiss discards the draft and calls no use case`() = runTest {
        val viewModel = buildViewModel()

        val state = send(
            viewModel,
            HistoryIntent.OnWriteNoteClick,
            HistoryIntent.OnNoteTextChange("borrador"),
            HistoryIntent.OnNoteDismiss,
        )

        assertNull(state.noteEditor)
        coVerify(exactly = 0) { addNote(any()) }
        coVerify(exactly = 0) { updateNote(any(), any()) }
        coVerify(exactly = 0) { deleteNote(any()) }
    }
}

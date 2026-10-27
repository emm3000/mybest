package com.emm.mybest.features.history.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.GetLogbookUseCase
import com.emm.mybest.domain.usecase.history.HistoryResult
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.photo.ReplacePhotoResult
import com.emm.mybest.domain.usecase.photo.ReplacePhotoUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val CLOCK = AppDayClock(FixedClock(LocalDateTime(2026, 5, 15, 12, 0).toInstant(LIMA)), LIMA)
private const val PHOTO_ID = "p1"
private const val NEW_URI = "content://new"

class HistoryViewModelPhotoTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val deletePhoto = mockk<DeletePhotoUseCase>(relaxed = true)
    private val replacePhoto = mockk<ReplacePhotoUseCase>()

    private fun buildViewModel(): HistoryViewModel {
        val getHistory = mockk<GetHistoryUseCase>()
        every { getHistory(any()) } returns flowOf(HistoryResult(emptyMap(), 0, 0, 0, 0))
        val getLogbook = mockk<GetLogbookUseCase>()
        every { getLogbook(any()) } returns flowOf(emptyList())
        return HistoryViewModel(
            getHistory,
            getLogbook,
            mockk<DeleteWeightByDateUseCase>(relaxed = true),
            HistoryPhotoUseCases(deletePhoto, replacePhoto),
            HistoryNoteUseCases(mockk(), mockk(), mockk()),
            CLOCK,
        )
    }

    @Test
    fun `a captured replacement replaces the photo once and reports it`() = runTest {
        coEvery { replacePhoto(any(), any()) } returns ReplacePhotoResult.Replaced
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(HistoryIntent.OnPhotoReplacementCaptured(PHOTO_ID, NEW_URI))
            advanceUntilIdle()

            assertEquals(HistoryEffect.PhotoReplaced, awaitItem())
            expectNoEvents()
        }
        coVerify(exactly = 1) { replacePhoto(PHOTO_ID, NEW_URI) }
    }

    @Test
    fun `a replacement of a photo that is gone reports it missing`() = runTest {
        coEvery { replacePhoto(any(), any()) } returns ReplacePhotoResult.PhotoNotFound
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(HistoryIntent.OnPhotoReplacementCaptured(PHOTO_ID, NEW_URI))
            advanceUntilIdle()

            assertEquals(HistoryEffect.PhotoNotFound, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `a replacement that throws reports the failure`() = runTest {
        coEvery { replacePhoto(any(), any()) } throws IllegalStateException("db")
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(HistoryIntent.OnPhotoReplacementCaptured(PHOTO_ID, NEW_URI))
            advanceUntilIdle()

            assertEquals(HistoryEffect.PhotoReplaceFailed, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `a cancelled replacement reports nothing`() = runTest {
        coEvery { replacePhoto(any(), any()) } throws CancellationException("cancelled")
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(HistoryIntent.OnPhotoReplacementCaptured(PHOTO_ID, NEW_URI))
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `OnDeletePhoto still deletes the photo once`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(HistoryIntent.OnDeletePhoto(PHOTO_ID))
        advanceUntilIdle()

        coVerify(exactly = 1) { deletePhoto(PHOTO_ID) }
    }
}

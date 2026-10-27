package com.emm.mybest.features.photo.presentation.viewer

import app.cash.turbine.test
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.photo.ReplacePhotoResult
import com.emm.mybest.domain.usecase.photo.ReplacePhotoUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private fun photoOf(id: String, createdAt: Long) = ProgressPhoto(
    id = id,
    date = LocalDate(2026, 11, 9),
    type = PhotoType.TRUNK,
    photoPath = "content://$id",
    createdAt = createdAt,
)

class PhotoViewerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observePhotosUseCase = mockk<ObservePhotosUseCase>().also {
        every { it() } returns flowOf(listOf(photoOf("old", 1_000L), photoOf("new", 2_000L)))
    }
    private val replacePhotoUseCase = mockk<ReplacePhotoUseCase>()

    private fun buildViewModel() = PhotoViewerViewModel("old", observePhotosUseCase, replacePhotoUseCase)

    private fun buildThreePhotoViewModel(initialPhotoId: String): PhotoViewerViewModel {
        every { observePhotosUseCase() } returns flowOf(
            listOf(photoOf("a", 1L), photoOf("b", 2L), photoOf("c", 3L)),
        )
        return PhotoViewerViewModel(initialPhotoId, observePhotosUseCase, replacePhotoUseCase)
    }

    @Test
    fun `state lists photos by creation stamp descending and keeps the initial photo`() = runTest {
        val state = buildViewModel().state.first { !it.isLoading }

        assertEquals(listOf("new", "old"), state.photos.map { it.id })
        assertEquals("old", state.initialPhotoId)
    }

    @Test
    fun `initial page is the index of the tapped photo in the sorted list`() = runTest {
        val state = buildThreePhotoViewModel("a").state.first { !it.isLoading }

        assertEquals(2, state.initialPage)
    }

    @Test
    fun `initial page is zero when the tapped photo is the newest`() = runTest {
        val state = buildThreePhotoViewModel("c").state.first { !it.isLoading }

        assertEquals(0, state.initialPage)
    }

    @Test
    fun `initial page is zero when the tapped photo is not in the list`() = runTest {
        val state = buildThreePhotoViewModel("gone").state.first { !it.isLoading }

        assertEquals(0, state.initialPage)
    }

    @Test
    fun `loading state starts on page zero`() = runTest {
        val state = buildThreePhotoViewModel("a").state.value

        assertEquals(true, state.isLoading)
        assertEquals(0, state.initialPage)
    }

    @Test
    fun `captured replacement is handed to the use case and announced`() = runTest {
        coEvery { replacePhotoUseCase("p1", "content://new") } returns ReplacePhotoResult.Replaced
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(PhotoViewerIntent.OnReplacementCaptured("p1", "content://new"))
            advanceUntilIdle()

            assertEquals(PhotoViewerEffect.PhotoReplaced, awaitItem())
            coVerify(exactly = 1) { replacePhotoUseCase("p1", "content://new") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `replacing a photo that no longer exists announces it`() = runTest {
        coEvery { replacePhotoUseCase(any(), any()) } returns ReplacePhotoResult.PhotoNotFound
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(PhotoViewerIntent.OnReplacementCaptured("gone", "content://new"))
            advanceUntilIdle()

            assertEquals(PhotoViewerEffect.PhotoNotFound, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a cancelled replacement announces nothing`() = runTest {
        coEvery { replacePhotoUseCase(any(), any()) } throws CancellationException("left the screen")
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(PhotoViewerIntent.OnReplacementCaptured("p1", "content://new"))
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failing replacement announces the failure`() = runTest {
        coEvery { replacePhotoUseCase(any(), any()) } throws IllegalStateException("disk full")
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(PhotoViewerIntent.OnReplacementCaptured("p1", "content://new"))
            advanceUntilIdle()

            assertEquals(PhotoViewerEffect.ReplaceFailed, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

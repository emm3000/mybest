package com.emm.mybest.features.photo.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ComparePhotosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<PhotoRepository>()
    private val face = ProgressPhoto(
        id = "p-face",
        date = LocalDate(2026, 3, 8),
        type = PhotoType.FACE,
        photoPath = "/tmp/face.jpg",
        createdAt = 1L,
    )
    private val body = ProgressPhoto(
        id = "p-body",
        date = LocalDate(2026, 3, 8),
        type = PhotoType.BODY,
        photoPath = "/tmp/body.jpg",
        createdAt = 2L,
    )
    private val dinner = ProgressPhoto(
        id = "p-dinner",
        date = LocalDate(2026, 3, 9),
        type = PhotoType.DINNER,
        photoPath = "/tmp/dinner.jpg",
        createdAt = 3L,
    )

    @Test
    fun `state emits initial photos from repository`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(face, body))
        every { repository.getPhotosByType(any()) } returns flowOf(listOf(face))
        val viewModel = ComparePhotosViewModel(repository)

        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            val loaded = awaitItem()
            assertEquals(listOf(face, body), loaded.photos)
            assertEquals(null, loaded.selectedType)
            assertEquals(face, loaded.beforePhoto)
            assertEquals(body, loaded.afterPhoto)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnTypeSelected filters photos and preselects available comparison pair`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(face, body))
        every { repository.getPhotosByType(PhotoType.FACE) } returns flowOf(listOf(face))
        every { repository.getPhotosByType(PhotoType.BODY) } returns flowOf(listOf(body))
        val viewModel = ComparePhotosViewModel(repository)

        viewModel.state.test {
            awaitItem() // loading
            awaitItem() // initial loaded

            viewModel.onIntent(ComparePhotosIntent.OnTypeSelected(PhotoType.FACE))
            val filtered = awaitState {
                it.selectedType == PhotoType.FACE &&
                    it.photos == listOf(face) &&
                    it.beforePhoto == face &&
                    it.afterPhoto == null
            }
            assertEquals(listOf(face), filtered.photos)
            assertEquals(face, filtered.beforePhoto)
            assertEquals(null, filtered.afterPhoto)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selection keeps manual choices while they remain available`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(face, body, dinner))
        every { repository.getPhotosByType(any()) } returns flowOf(listOf(face, body, dinner))
        val viewModel = ComparePhotosViewModel(repository)

        viewModel.state.test {
            awaitItem()
            awaitState { it.beforePhoto == face && it.afterPhoto == dinner }

            viewModel.onIntent(ComparePhotosIntent.OnAfterPhotoSelected(body))
            awaitState { it.beforePhoto == face && it.afterPhoto == body }

            viewModel.onIntent(ComparePhotosIntent.OnBeforePhotoSelected(dinner))

            val manualSelection = awaitState {
                it.beforePhoto == dinner && it.afterPhoto == body
            }
            assertEquals(dinner, manualSelection.beforePhoto)
            assertEquals(body, manualSelection.afterPhoto)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleSwap exchanges before and after photos`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(face, body))
        every { repository.getPhotosByType(any()) } returns flowOf(listOf(face))
        val viewModel = ComparePhotosViewModel(repository)

        viewModel.state.test {
            awaitItem() // loading
            awaitState { it.beforePhoto == face && it.afterPhoto == body }

            viewModel.onIntent(ComparePhotosIntent.ToggleSwap)
            val swapped = awaitState { it.beforePhoto == body && it.afterPhoto == face }
            assertEquals(body, swapped.beforePhoto)
            assertEquals(face, swapped.afterPhoto)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting the same photo in both slots emits error and keeps previous selection`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(face, body))
        every { repository.getPhotosByType(any()) } returns flowOf(listOf(face))
        val viewModel = ComparePhotosViewModel(repository)

        viewModel.state.test {
            awaitItem()
            val initialLoaded = awaitItem()
            assertEquals(face, initialLoaded.beforePhoto)
            assertEquals(body, initialLoaded.afterPhoto)

            viewModel.effect.test {
                viewModel.onIntent(ComparePhotosIntent.OnAfterPhotoSelected(face))
                advanceUntilIdle()

                assertEquals(
                    ComparePhotosEffect.ShowError("Elige una foto distinta para DESPUÉS."),
                    awaitItem(),
                )
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(face, viewModel.state.value.beforePhoto)
            assertEquals(body, viewModel.state.value.afterPhoto)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private suspend fun ReceiveTurbine<ComparePhotosState>.awaitState(
    predicate: (ComparePhotosState) -> Boolean,
): ComparePhotosState {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}

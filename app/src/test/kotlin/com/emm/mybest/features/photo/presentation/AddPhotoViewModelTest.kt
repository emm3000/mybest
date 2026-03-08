package com.emm.mybest.features.photo.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddPhotoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<PhotoRepository>()

    @Test
    fun `OnPhotosSelected and OnTypeSelected update selection state`() {
        val viewModel = AddPhotoViewModel(repository)

        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg", "b.jpg")))
        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(1, PhotoType.BODY))

        val state = viewModel.state.value
        assertEquals(2, state.selectedPhotos.size)
        assertEquals(PhotoType.FACE, state.selectedPhotos[0].type)
        assertEquals(PhotoType.BODY, state.selectedPhotos[1].type)
    }

    @Test
    fun `OnRemovePhoto removes selected photo by index`() {
        val viewModel = AddPhotoViewModel(repository)
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg", "b.jpg")))

        viewModel.onIntent(AddPhotoIntent.OnRemovePhoto(0))

        assertEquals(listOf("b.jpg"), viewModel.state.value.selectedPhotos.map { it.uri })
    }

    @Test
    fun `OnSaveClick emits error when there are no selected photos`() = runTest {
        val viewModel = AddPhotoViewModel(repository)

        viewModel.effect.test {
            viewModel.onIntent(AddPhotoIntent.OnSaveClick)
            assertEquals(
                AddPhotoEffect.ShowError("Debes seleccionar al menos una foto"),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSaveClick success maps and saves photos then navigates back`() = runTest {
        val captured = slot<List<NewProgressPhoto>>()
        coEvery { repository.savePhotos(capture(captured)) } returns Unit
        val viewModel = AddPhotoViewModel(repository)
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("a.jpg")))
        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(0, PhotoType.BODY))

        viewModel.effect.test {
            viewModel.onIntent(AddPhotoIntent.OnSaveClick)
            advanceUntilIdle()

            assertEquals(AddPhotoEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.savePhotos(any()) }
        assertEquals(1, captured.captured.size)
        assertEquals("a.jpg", captured.captured.first().photoPath)
        assertEquals(PhotoType.BODY, captured.captured.first().type)
        assertEquals(false, viewModel.state.value.isLoading)
    }
}

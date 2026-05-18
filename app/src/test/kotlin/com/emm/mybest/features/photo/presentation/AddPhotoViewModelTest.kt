package com.emm.mybest.features.photo.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddPhotoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val photoRepository = mockk<PhotoRepository>(relaxed = true)

    private fun buildViewModel() = AddPhotoViewModel(photoRepository)

    @Test
    fun `initial state has empty selectedPhotos and isLoading false`() = runTest {
        val viewModel = buildViewModel()

        val state = viewModel.state.value
        assertTrue(state.selectedPhotos.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `OnPhotosSelected appends new SelectedPhotos with default FACE type`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/photo1.jpg", "/tmp/photo2.jpg")))

        val state = viewModel.state.value
        assertEquals(2, state.selectedPhotos.size)
        assertEquals("/tmp/photo1.jpg", state.selectedPhotos[0].uri)
        assertEquals(PhotoType.FACE, state.selectedPhotos[0].type)
        assertEquals("/tmp/photo2.jpg", state.selectedPhotos[1].uri)
    }

    @Test
    fun `OnPhotosSelected appends to existing photos`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/a.jpg")))
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/b.jpg")))

        assertEquals(2, viewModel.state.value.selectedPhotos.size)
    }

    @Test
    fun `OnTypeSelected updates type at the given index`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/a.jpg", "/tmp/b.jpg")))

        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(index = 0, type = PhotoType.TRUNK))

        val photos = viewModel.state.value.selectedPhotos
        assertEquals(PhotoType.TRUNK, photos[0].type)
        assertEquals(PhotoType.FACE, photos[1].type)
    }

    @Test
    fun `OnTypeSelected with out-of-range index does nothing`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/a.jpg")))

        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(index = 5, type = PhotoType.TRUNK))

        assertEquals(PhotoType.FACE, viewModel.state.value.selectedPhotos[0].type)
    }

    @Test
    fun `OnRemovePhoto removes photo at given index`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/a.jpg", "/tmp/b.jpg")))

        viewModel.onIntent(AddPhotoIntent.OnRemovePhoto(index = 0))

        val photos = viewModel.state.value.selectedPhotos
        assertEquals(1, photos.size)
        assertEquals("/tmp/b.jpg", photos[0].uri)
    }

    @Test
    fun `OnRemovePhoto with out-of-range index does nothing`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/a.jpg")))

        viewModel.onIntent(AddPhotoIntent.OnRemovePhoto(index = 10))

        assertEquals(1, viewModel.state.value.selectedPhotos.size)
    }

    @Test
    fun `OnSaveClick with empty selection emits ShowError without calling repository`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(AddPhotoIntent.OnSaveClick)
            advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is AddPhotoEffect.ShowError)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { photoRepository.savePhotos(any()) }
    }

    @Test
    fun `OnSaveClick with photos calls repository and emits NavigateBack on success`() = runTest {
        coEvery { photoRepository.savePhotos(any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/face.jpg")))
        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(0, PhotoType.FACE))

        viewModel.effect.test {
            viewModel.onIntent(AddPhotoIntent.OnSaveClick)
            advanceUntilIdle()
            assertEquals(AddPhotoEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) {
            photoRepository.savePhotos(
                listOf(NewProgressPhoto(type = PhotoType.FACE, photoPath = "/tmp/face.jpg")),
            )
        }
    }

    @Test
    fun `OnSaveClick resets isLoading to false after save`() = runTest {
        coEvery { photoRepository.savePhotos(any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/face.jpg")))

        viewModel.onIntent(AddPhotoIntent.OnSaveClick)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `OnSaveClick emits ShowError when repository throws`() = runTest {
        coEvery { photoRepository.savePhotos(any()) } throws RuntimeException("DB error")
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/face.jpg")))

        viewModel.effect.test {
            viewModel.onIntent(AddPhotoIntent.OnSaveClick)
            advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is AddPhotoEffect.ShowError)
            assertTrue((effect as AddPhotoEffect.ShowError).message.contains("DB error"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSaveClick passes correct PhotoType to repository`() = runTest {
        coEvery { photoRepository.savePhotos(any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddPhotoIntent.OnPhotosSelected(listOf("/tmp/trunk.jpg")))
        viewModel.onIntent(AddPhotoIntent.OnTypeSelected(0, PhotoType.TRUNK))

        viewModel.onIntent(AddPhotoIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify {
            photoRepository.savePhotos(
                listOf(NewProgressPhoto(type = PhotoType.TRUNK, photoPath = "/tmp/trunk.jpg")),
            )
        }
    }
}

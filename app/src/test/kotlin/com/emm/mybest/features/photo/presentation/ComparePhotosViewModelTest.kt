package com.emm.mybest.features.photo.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

private fun makePhoto(id: String, type: PhotoType): ProgressPhoto = ProgressPhoto(
    id = id,
    date = LocalDate(2026, 1, 1),
    type = type,
    photoPath = "/tmp/$id.jpg",
    createdAt = System.currentTimeMillis(),
)

class ComparePhotosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<PhotoRepository>()

    private val beforePhoto = makePhoto("before-id", PhotoType.TRUNK)
    private val afterPhoto = makePhoto("after-id", PhotoType.TRUNK)

    private fun buildViewModel(
        beforeId: String = beforePhoto.id,
        afterId: String = afterPhoto.id,
    ) = ComparePhotosViewModel(beforeId = beforeId, afterId = afterId, photoRepository = repository)

    @Test
    fun `initial state is loading`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        assertEquals(true, viewModel.state.value.isLoading)
    }

    @Test
    fun `resolves before and after from repository by id`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(beforePhoto, afterPhoto))
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem() // loading
            val loaded = awaitItem()
            assertEquals(beforePhoto, loaded.before)
            assertEquals(afterPhoto, loaded.after)
            assertEquals(false, loaded.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `before is null when id not found in repository`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(afterPhoto))
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertNull(loaded.before)
            assertEquals(afterPhoto, loaded.after)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `after is null when id not found in repository`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(beforePhoto))
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(beforePhoto, loaded.before)
            assertNull(loaded.after)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Close intent emits NavigateBack effect`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(listOf(beforePhoto, afterPhoto))
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(ComparePhotosIntent.Close)
            advanceUntilIdle()
            assertEquals(ComparePhotosEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `both photos null when repository is empty`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(emptyList())
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertNull(loaded.before)
            assertNull(loaded.after)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

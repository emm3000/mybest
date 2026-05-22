package com.emm.mybest.features.photo.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.photo.GetPhotosOverviewUseCase
import com.emm.mybest.domain.usecase.photo.PhotoTypeOverview
import com.emm.mybest.domain.usecase.photo.PhotosOverview
import com.emm.mybest.domain.usecase.photo.SavePhotosUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

private val FIXED_CLOCK = object : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(1_779_019_200L)
}

private fun makePhoto(id: String, type: PhotoType, createdAt: Long): ProgressPhoto = ProgressPhoto(
    id = id,
    date = LocalDate(2026, 1, 1),
    type = type,
    photoPath = "/tmp/$id.jpg",
    createdAt = createdAt,
)

private fun emptyOverview() = PhotoTypeOverview(
    before = null,
    after = null,
    timeline = emptyList(),
    beforeWeightKg = null,
    afterWeightKg = null,
)

private fun overviewWith(before: ProgressPhoto?, after: ProgressPhoto?): PhotoTypeOverview =
    PhotoTypeOverview(
        before = before,
        after = after,
        timeline = listOfNotNull(before, after),
        beforeWeightKg = null,
        afterWeightKg = null,
    )

class PhotosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPhotosOverviewUseCase = mockk<GetPhotosOverviewUseCase>()
    private val savePhotosUseCase = mockk<SavePhotosUseCase>(relaxed = true)
    private val deletePhotoUseCase = mockk<DeletePhotoUseCase>(relaxed = true)

    private fun buildViewModel(): PhotosViewModel {
        val emptyOverviewMap = PhotoType.entries.associateWith { emptyOverview() }
        every { getPhotosOverviewUseCase.invoke() } returns MutableStateFlow(
            PhotosOverview(emptyOverviewMap),
        )
        return PhotosViewModel(
            getPhotosOverviewUseCase = getPhotosOverviewUseCase,
            savePhotosUseCase = savePhotosUseCase,
            deletePhotoUseCase = deletePhotoUseCase,
            clock = FIXED_CLOCK,
        )
    }

    @Test
    fun `initial state has loading true and TRUNK selected`() = runTest {
        val viewModel = buildViewModel()
        val initial = viewModel.state.value
        assertEquals(true, initial.isLoading)
        assertEquals(PhotoType.TRUNK, initial.selectedType)
    }

    @Test
    fun `SelectType switches state selectedType`() = runTest {
        val viewModel = buildViewModel()
        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(PhotosIntent.SelectType(PhotoType.FACE))
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(PhotoType.FACE, updated.selectedType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnPhotoCaptured calls savePhotosUseCase with current tab type`() = runTest {
        coEvery { savePhotosUseCase(any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PhotosIntent.OnPhotoCaptured("/tmp/photo.jpg"))
        advanceUntilIdle()

        coVerify {
            savePhotosUseCase(
                listOf(NewProgressPhoto(photoPath = "/tmp/photo.jpg", type = PhotoType.TRUNK)),
            )
        }
    }

    @Test
    fun `OnPhotoCaptured with FACE tab saves with FACE type`() = runTest {
        coEvery { savePhotosUseCase(any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PhotosIntent.SelectType(PhotoType.FACE))
        viewModel.onIntent(PhotosIntent.OnPhotoCaptured("/tmp/photo.jpg"))
        advanceUntilIdle()

        coVerify {
            savePhotosUseCase(
                listOf(NewProgressPhoto(photoPath = "/tmp/photo.jpg", type = PhotoType.FACE)),
            )
        }
    }

    @Test
    fun `DeletePhoto calls deletePhotoUseCase with id`() = runTest {
        coEvery { deletePhotoUseCase(any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PhotosIntent.DeletePhoto("photo-123"))
        advanceUntilIdle()

        coVerify { deletePhotoUseCase("photo-123") }
    }

    @Test
    fun `OpenCompare emits NavigateToCompare when both before and after exist`() = runTest {
        val beforePhoto = makePhoto("before-id", PhotoType.TRUNK, 100)
        val afterPhoto = makePhoto("after-id", PhotoType.TRUNK, 200)
        val overviewMap = PhotoType.entries.associateWith { type ->
            if (type == PhotoType.TRUNK) overviewWith(beforePhoto, afterPhoto) else emptyOverview()
        }
        every { getPhotosOverviewUseCase.invoke() } returns MutableStateFlow(
            PhotosOverview(overviewMap),
        )

        val viewModel = PhotosViewModel(
            getPhotosOverviewUseCase = getPhotosOverviewUseCase,
            savePhotosUseCase = savePhotosUseCase,
            deletePhotoUseCase = deletePhotoUseCase,
            clock = FIXED_CLOCK,
        )

        viewModel.state.test {
            advanceUntilIdle()
            skipItems(2)

            viewModel.effect.test {
                viewModel.onIntent(PhotosIntent.OpenCompare)
                advanceUntilIdle()
                val effect = awaitItem()
                assertEquals(PhotosEffect.NavigateToCompare("before-id", "after-id"), effect)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OpenCompare does not emit when before is null`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(PhotosIntent.OpenCompare)
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty state shows zero count for all types`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(0, state.countByType[PhotoType.TRUNK] ?: 0)
        assertEquals(0, state.countByType[PhotoType.FACE] ?: 0)
    }
}

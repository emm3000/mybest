package com.emm.mybest.features.photo.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.photo.GetPhotosOverviewUseCase
import com.emm.mybest.domain.usecase.photo.PhotoTypeOverview
import com.emm.mybest.domain.usecase.photo.PhotosOverview
import com.emm.mybest.domain.usecase.photo.SavePhotosUseCase
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val SATURDAY_TWO_AM = LocalDateTime(2026, 10, 24, 2, 0).toInstant(LIMA)
private val FRIDAY = LocalDate(2026, 10, 23)
private val APP_DAY_CLOCK = AppDayClock(FixedClock(SATURDAY_TWO_AM), LIMA)

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

private suspend fun PhotosViewModel.selectedTypeNow(): PhotoType =
    state.first { !it.isLoading }.selectedType

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
            appDayClock = APP_DAY_CLOCK,
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
    fun `a capture of FACE saves FACE and selects FACE on a fresh ViewModel`() = runTest {
        coEvery { savePhotosUseCase(any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PhotosIntent.OnPhotoCaptured("/tmp/photo.jpg", PhotoType.FACE))
        advanceUntilIdle()

        coVerify {
            savePhotosUseCase(
                listOf(NewProgressPhoto(photoPath = "/tmp/photo.jpg", type = PhotoType.FACE, date = FRIDAY)),
            )
        }
        assertEquals(PhotoType.FACE, viewModel.selectedTypeNow())
    }

    @Test
    fun `a capture of TRUNK saves TRUNK and selects TRUNK while FACE is selected`() = runTest {
        coEvery { savePhotosUseCase(any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PhotosIntent.SelectType(PhotoType.FACE))
        viewModel.onIntent(PhotosIntent.OnPhotoCaptured("/tmp/photo.jpg", PhotoType.TRUNK))
        advanceUntilIdle()

        coVerify {
            savePhotosUseCase(
                listOf(NewProgressPhoto(photoPath = "/tmp/photo.jpg", type = PhotoType.TRUNK, date = FRIDAY)),
            )
        }
        assertEquals(PhotoType.TRUNK, viewModel.selectedTypeNow())
    }

    @Test
    fun `a progress photo saved at 02_00 gets the previous app day`() = runTest {
        coEvery { savePhotosUseCase(any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(PhotosIntent.OnPhotoCaptured("/tmp/photo.jpg", PhotoType.TRUNK))
        advanceUntilIdle()

        coVerify { savePhotosUseCase(match { photos -> photos.single().date == FRIDAY }) }
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
    fun `a cancelled save emits no error`() = runTest {
        coEvery { savePhotosUseCase(any()) } throws CancellationException("cancelled")
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(PhotosIntent.OnPhotoCaptured("/tmp/photo.jpg", PhotoType.TRUNK))
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a cancelled delete emits no error`() = runTest {
        coEvery { deletePhotoUseCase(any()) } throws CancellationException("cancelled")
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(PhotosIntent.DeletePhoto("photo-123"))
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed save emits one ShowError with the cause`() = runTest {
        coEvery { savePhotosUseCase(any()) } throws IllegalStateException("db")
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(PhotosIntent.OnPhotoCaptured("/tmp/photo.jpg", PhotoType.TRUNK))
            advanceUntilIdle()
            assertEquals(PhotosEffect.ShowError("Error al guardar: db"), awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed delete emits one ShowError with the cause`() = runTest {
        coEvery { deletePhotoUseCase(any()) } throws IllegalStateException("db")
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(PhotosIntent.DeletePhoto("photo-123"))
            advanceUntilIdle()
            assertEquals(PhotosEffect.ShowError("Error al eliminar: db"), awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
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
            appDayClock = APP_DAY_CLOCK,
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

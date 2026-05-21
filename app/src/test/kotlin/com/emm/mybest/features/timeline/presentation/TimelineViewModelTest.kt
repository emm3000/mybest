package com.emm.mybest.features.timeline.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.usecase.history.GetTimelineUseCase
import com.emm.mybest.domain.usecase.history.TimelineResult
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TimelineViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTimelineUseCase = mockk<GetTimelineUseCase>(relaxed = true)
    private val deletePhotoUseCase = mockk<DeletePhotoUseCase>(relaxed = true)

    private val dayOne = LocalDate(2026, 3, 8)
    private val dayTwo = LocalDate(2026, 3, 9)
    private val photos = listOf(
        ProgressPhoto("p1", date = dayOne, type = PhotoType.FACE, photoPath = "/tmp/1", createdAt = 1L),
        ProgressPhoto("p2", date = dayOne, type = PhotoType.TRUNK, photoPath = "/tmp/2", createdAt = 2L),
        ProgressPhoto("p3", date = dayTwo, type = PhotoType.TRUNK, photoPath = "/tmp/3", createdAt = 3L),
    )

    private fun buildTimeline(photoList: List<ProgressPhoto> = photos): TimelineResult {
        val sorted = photoList.sortedByDescending { it.createdAt }
        return TimelineResult(
            photosByDate = photoList.groupBy { it.date },
            photosByMonth = sorted.groupBy { YearMonthValue.from(it.date) },
        )
    }

    @Test
    fun `state groups photos by date`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline())
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.photosByDate.size)
            assertEquals(2, loaded.photosByDate[dayOne]?.size)
            assertEquals(1, loaded.photosByDate[dayTwo]?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state groups photos by month`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline())
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.state.test {
            awaitItem() // loading
            val loaded = awaitItem()
            val expectedMonth = YearMonthValue(2026, 3)
            assertEquals(1, loaded.photosByMonth.size)
            assertEquals(3, loaded.photosByMonth[expectedMonth]?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnBackClick emits NavigateBack effect`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline(emptyList()))
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.effect.test {
            viewModel.onIntent(TimelineIntent.OnBackClick)
            assertEquals(TimelineEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `EnterSelection sets selectionMode true and adds photo`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline())
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.state.test {
            awaitItem() // loading
            awaitItem() // loaded

            viewModel.onIntent(TimelineIntent.EnterSelection("p1"))
            val selState = awaitItem()
            assertTrue(selState.selectionMode)
            assertEquals(setOf("p1"), selState.selectedIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleSelection deselects already selected photo`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline())
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.state.test {
            awaitItem() // loading
            awaitItem() // loaded

            viewModel.onIntent(TimelineIntent.EnterSelection("p1"))
            awaitItem() // selectionMode = true, selectedIds = {p1}

            viewModel.onIntent(TimelineIntent.ToggleSelection("p1"))
            val deselected = awaitItem()
            assertFalse(deselected.selectionMode)
            assertTrue(deselected.selectedIds.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleSelection adds second photo to selection`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline())
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.state.test {
            awaitItem() // loading
            awaitItem() // loaded

            viewModel.onIntent(TimelineIntent.EnterSelection("p1"))
            awaitItem() // {p1}

            viewModel.onIntent(TimelineIntent.ToggleSelection("p2"))
            val twoSelected = awaitItem()
            assertEquals(setOf("p1", "p2"), twoSelected.selectedIds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ExitSelection clears selection mode`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline())
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(TimelineIntent.EnterSelection("p1"))
            awaitItem()

            viewModel.onIntent(TimelineIntent.ExitSelection)
            val cleared = awaitItem()
            assertFalse(cleared.selectionMode)
            assertTrue(cleared.selectedIds.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DeleteSelected calls DeletePhotoUseCase for each selected id`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline())
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(TimelineIntent.EnterSelection("p1"))
            awaitItem()
            viewModel.onIntent(TimelineIntent.ToggleSelection("p2"))
            awaitItem()

            viewModel.onIntent(TimelineIntent.DeleteSelected)
            // After delete the selection is cleared.
            val afterDelete = awaitItem()
            assertFalse(afterDelete.selectionMode)

            coVerify { deletePhotoUseCase("p1") }
            coVerify { deletePhotoUseCase("p2") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `CompareSelected emits NavigateToCompare effect`() = runTest {
        every { getTimelineUseCase() } returns flowOf(buildTimeline(emptyList()))
        val viewModel = TimelineViewModel(getTimelineUseCase, deletePhotoUseCase)

        viewModel.effect.test {
            viewModel.onIntent(TimelineIntent.CompareSelected)
            assertEquals(TimelineEffect.NavigateToCompare, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

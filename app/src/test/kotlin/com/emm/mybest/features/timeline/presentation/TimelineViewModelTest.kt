package com.emm.mybest.features.timeline.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<PhotoRepository>()

    @Test
    fun `state groups photos by date`() = runTest {
        val dayOne = LocalDate(2026, 3, 8)
        val dayTwo = LocalDate(2026, 3, 9)
        val photos = listOf(
            ProgressPhoto("p1", date = dayOne, type = PhotoType.FACE, photoPath = "/tmp/1", createdAt = 1L),
            ProgressPhoto("p2", date = dayOne, type = PhotoType.BODY, photoPath = "/tmp/2", createdAt = 2L),
            ProgressPhoto("p3", date = dayTwo, type = PhotoType.BODY, photoPath = "/tmp/3", createdAt = 3L),
        )
        every { repository.getAllPhotos() } returns flowOf(photos)
        val viewModel = TimelineViewModel(repository)

        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            assertEquals(2, loaded.photosByDate.size)
            assertEquals(2, loaded.photosByDate[dayOne]?.size)
            assertEquals(1, loaded.photosByDate[dayTwo]?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnBackClick emits NavigateBack effect`() = runTest {
        every { repository.getAllPhotos() } returns flowOf(emptyList())
        val viewModel = TimelineViewModel(repository)

        viewModel.effect.test {
            viewModel.onIntent(TimelineIntent.OnBackClick)
            assertEquals(TimelineEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

private val FIXED_DATE = LocalDate(2026, 5, 17)
private val FIXED_CLOCK = object : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(1_779_019_200L)
}

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeWeightProgress: ObserveWeightProgressUseCase = mockk()
    private val observePhotos: ObservePhotosUseCase = mockk()
    private val useCases: HomeUseCases = HomeUseCases(
        observeWeightProgress = observeWeightProgress,
        observePhotos = observePhotos,
    )

    private fun buildViewModel(
        weights: List<WeightEntry> = emptyList(),
        photos: List<ProgressPhoto> = emptyList(),
    ): HomeViewModel {
        every { observeWeightProgress() } returns flowOf(weights)
        every { observePhotos() } returns flowOf(photos)
        return HomeViewModel(
            useCases = useCases,
            clock = FIXED_CLOCK,
        )
    }

    @Test
    fun `loaded state carries today and its ISO week number`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(FIXED_DATE, state.today)
        assertEquals(20, state.weekNumber)
    }

    @Test
    fun `empty weights produces null lastWeightKg and null previousWeightKg`() = runTest {
        val viewModel = buildViewModel(weights = emptyList())
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.lastWeightKg)
        assertNull(state.previousWeightKg)
    }

    @Test
    fun `two weights produces both lastWeightKg and previousWeightKg`() = runTest {
        val weights = listOf(
            WeightEntry(id = "1", date = FIXED_DATE.minus(DatePeriod(days = 1)), weight = 80.0f),
            WeightEntry(id = "2", date = FIXED_DATE, weight = 79.5f),
        )
        val viewModel = buildViewModel(weights = weights)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(79.5f, state.lastWeightKg)
        assertEquals(80.0f, state.previousWeightKg)
    }

    @Test
    fun `photos with type TRUNK and last 4 days ago produces correct state`() = runTest {
        val photoDate = FIXED_DATE.minus(DatePeriod(days = 4))
        val photos = listOf(
            ProgressPhoto(
                id = "p1",
                date = photoDate,
                type = PhotoType.TRUNK,
                photoPath = "/path/photo.jpg",
                createdAt = 0L,
            ),
        )
        val viewModel = buildViewModel(photos = photos)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PhotoType.TRUNK, state.lastPhotoType)
        assertEquals(4, state.lastPhotoDaysAgo)
    }
}

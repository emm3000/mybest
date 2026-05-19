package com.emm.mybest.features.history.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val weightRepository = mockk<WeightRepository>(relaxed = true)
    private val photoRepository = mockk<PhotoRepository>(relaxed = true)

    private val fixedMonth = YearMonthValue(2026, 3)

    private val weightEntry = WeightEntry(
        id = "w1",
        date = LocalDate(2026, 3, 10),
        weight = 75f,
        note = null,
    )
    private val photo = ProgressPhoto(
        id = "p1",
        date = LocalDate(2026, 3, 10),
        type = PhotoType.FACE,
        photoPath = "/tmp/face.jpg",
        createdAt = 1L,
    )

    private fun buildViewModel(
        weights: List<WeightEntry> = emptyList(),
        photos: List<ProgressPhoto> = emptyList(),
        initialMonth: YearMonthValue = fixedMonth,
    ): HistoryViewModel {
        every { weightRepository.getWeightProgress() } returns flowOf(weights)
        every { photoRepository.getAllPhotos() } returns flowOf(photos)
        return HistoryViewModel(weightRepository, photoRepository, initialMonth)
    }

    @Test
    fun `initial state has isLoading true before data arrives`() = runTest {
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())
        val viewModel = HistoryViewModel(weightRepository, photoRepository, fixedMonth)

        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loaded state with empty data has empty monthlyData`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem() // isLoading
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertTrue(loaded.monthlyData.isEmpty())
            assertNull(loaded.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `weight entry appears as DaySummary with hasWeight true`() = runTest {
        val viewModel = buildViewModel(weights = listOf(weightEntry))

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            val summary = loaded.monthlyData[weightEntry.date]
            assertTrue(summary?.hasWeight == true)
            assertFalse(summary?.hasPhoto == true)
            assertTrue(summary?.hasActivity == true)
            assertEquals(weightEntry, summary?.weight)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `photo entry appears as DaySummary with hasPhoto true`() = runTest {
        val viewModel = buildViewModel(photos = listOf(photo))

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            val summary = loaded.monthlyData[photo.date]
            assertTrue(summary?.hasPhoto == true)
            assertFalse(summary?.hasWeight == true)
            assertEquals(listOf(photo), summary?.photos)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `same date weight and photo are merged into single DaySummary`() = runTest {
        val viewModel = buildViewModel(weights = listOf(weightEntry), photos = listOf(photo))

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(1, loaded.monthlyData.size)
            val summary = loaded.monthlyData[LocalDate(2026, 3, 10)]
            assertTrue(summary?.hasWeight == true)
            assertTrue(summary?.hasPhoto == true)
            assertTrue(summary?.hasActivity == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDateSelected sets selectedDate in state`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDateSelected(LocalDate(2026, 3, 15)))
            val updated = awaitItem()
            assertEquals(LocalDate(2026, 3, 15), updated.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDateDismiss clears selectedDate`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDateSelected(LocalDate(2026, 3, 15)))
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDateDismiss)
            val cleared = awaitItem()
            assertNull(cleared.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnMonthChange updates selectedMonth in state`() = runTest {
        val viewModel = buildViewModel()
        val newMonth = YearMonthValue(2026, 4)

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnMonthChange(newMonth))
            val updated = awaitItem()
            assertEquals(newMonth, updated.selectedMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRangeChange updates selectedRange in state`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnRangeChange(HistoryRange.YEAR))
            val updated = awaitItem()
            assertEquals(HistoryRange.YEAR, updated.selectedRange)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDeleteWeight calls repository deleteByDate`() = runTest {
        val viewModel = buildViewModel(weights = listOf(weightEntry))

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDeleteWeight(weightEntry.date))
            advanceUntilIdle()

            coVerify(exactly = 1) { weightRepository.deleteByDate(weightEntry.date) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDeletePhoto calls repository deletePhoto`() = runTest {
        val viewModel = buildViewModel(photos = listOf(photo))

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDeletePhoto(photo.id))
            advanceUntilIdle()

            coVerify(exactly = 1) { photoRepository.deletePhoto(photo.id) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state updates reactively when repository emits new data`() = runTest {
        val weightFlow = MutableStateFlow(emptyList<WeightEntry>())
        every { weightRepository.getWeightProgress() } returns weightFlow
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())
        val viewModel = HistoryViewModel(weightRepository, photoRepository, fixedMonth)

        viewModel.state.test {
            awaitItem() // loading
            val empty = awaitItem()
            assertTrue(empty.monthlyData.isEmpty())

            weightFlow.value = listOf(weightEntry)
            val updated = awaitItem()
            assertEquals(1, updated.monthlyData.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `weightTrend is populated from weight entries in selected range`() = runTest {
        val viewModel = buildViewModel(weights = listOf(weightEntry))

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            // MONTH range, March 2026 — weightEntry date is 2026-03-10, which is in March
            assertEquals(1, loaded.weightTrend.size)
            assertEquals(weightEntry.date, loaded.weightTrend[0].date)
            assertEquals(weightEntry.weight, loaded.weightTrend[0].weight)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // region streak computation

    @Test
    fun `computeStreak returns 0 for empty data`() {
        val rangeDates = setOf(
            LocalDate(2026, 3, 1),
            LocalDate(2026, 3, 2),
        )
        assertEquals(0, computeStreak(rangeDates, emptyMap()))
    }

    @Test
    fun `computeStreak returns 1 for single active day`() {
        val date = LocalDate(2026, 3, 10)
        val summary = DaySummary(date, weight = weightEntry)
        assertEquals(1, computeStreak(setOf(date), mapOf(date to summary)))
    }

    @Test
    fun `computeStreak returns correct longest run for non-consecutive days`() {
        val dates = (1..7).map { LocalDate(2026, 3, it) }.toSet()
        val data = mapOf(
            LocalDate(2026, 3, 1) to DaySummary(LocalDate(2026, 3, 1), weight = weightEntry),
            LocalDate(2026, 3, 2) to DaySummary(LocalDate(2026, 3, 2), weight = weightEntry),
            // gap on 3rd
            LocalDate(2026, 3, 4) to DaySummary(LocalDate(2026, 3, 4), weight = weightEntry),
            LocalDate(2026, 3, 5) to DaySummary(LocalDate(2026, 3, 5), weight = weightEntry),
            LocalDate(2026, 3, 6) to DaySummary(LocalDate(2026, 3, 6), weight = weightEntry),
        )
        assertEquals(3, computeStreak(dates, data))
    }

    @Test
    fun `computeStreak returns total count when all days are active`() {
        val dates = (1..5).map { LocalDate(2026, 3, it) }.toSet()
        val data = dates.associateWith { DaySummary(it, weight = weightEntry) }
        assertEquals(5, computeStreak(dates, data))
    }

    // endregion
}

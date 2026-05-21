package com.emm.mybest.features.history.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.HistoryRange
import com.emm.mybest.domain.usecase.history.HistoryResult
import com.emm.mybest.domain.usecase.history.WeightTrendPoint
import com.emm.mybest.domain.usecase.history.computeStreak
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getHistoryUseCase = mockk<GetHistoryUseCase>(relaxed = true)
    private val deleteWeightByDate = mockk<DeleteWeightByDateUseCase>(relaxed = true)
    private val deletePhoto = mockk<DeletePhotoUseCase>(relaxed = true)

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

    private fun emptyResult() = HistoryResult(
        monthlyData = emptyMap(),
        weightTrend = emptyList(),
        streak = 0,
        activeDays = 0,
    )

    private fun buildViewModel(
        result: HistoryResult = emptyResult(),
        initialMonth: YearMonthValue = fixedMonth,
    ): HistoryViewModel {
        every { getHistoryUseCase(any(), any()) } returns flowOf(result)
        return HistoryViewModel(getHistoryUseCase, deleteWeightByDate, deletePhoto, initialMonth)
    }

    @Test
    fun `initial state has isLoading true before data arrives`() = runTest {
        every { getHistoryUseCase(any(), any()) } returns flowOf(emptyResult())
        val viewModel = HistoryViewModel(getHistoryUseCase, deleteWeightByDate, deletePhoto, fixedMonth)

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
    fun `weight entry in result appears as DaySummary with hasWeight true`() = runTest {
        val summary = DaySummary(weightEntry.date, weight = weightEntry)
        val result = HistoryResult(
            monthlyData = mapOf(weightEntry.date to summary),
            weightTrend = listOf(WeightTrendPoint(weightEntry.date, weightEntry.weight)),
            streak = 1,
            activeDays = 1,
        )
        val viewModel = buildViewModel(result)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            val s = loaded.monthlyData[weightEntry.date]
            assertTrue(s?.hasWeight == true)
            assertFalse(s?.hasPhoto == true)
            assertTrue(s?.hasActivity == true)
            assertEquals(weightEntry, s?.weight)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `photo entry in result appears as DaySummary with hasPhoto true`() = runTest {
        val summary = DaySummary(photo.date, photos = listOf(photo))
        val result = HistoryResult(
            monthlyData = mapOf(photo.date to summary),
            weightTrend = emptyList(),
            streak = 1,
            activeDays = 1,
        )
        val viewModel = buildViewModel(result)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            val s = loaded.monthlyData[photo.date]
            assertTrue(s?.hasPhoto == true)
            assertFalse(s?.hasWeight == true)
            assertEquals(listOf(photo), s?.photos)
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
    fun `OnDeleteWeight calls DeleteWeightByDateUseCase`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDeleteWeight(weightEntry.date))
            advanceUntilIdle()

            coVerify(exactly = 1) { deleteWeightByDate(weightEntry.date) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDeletePhoto calls DeletePhotoUseCase`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDeletePhoto(photo.id))
            advanceUntilIdle()

            coVerify(exactly = 1) { deletePhoto(photo.id) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state updates reactively when use case emits new data`() = runTest {
        val resultFlow = MutableStateFlow(emptyResult())
        every { getHistoryUseCase(any(), any()) } returns resultFlow
        val viewModel = HistoryViewModel(getHistoryUseCase, deleteWeightByDate, deletePhoto, fixedMonth)

        viewModel.state.test {
            awaitItem() // loading
            val empty = awaitItem()
            assertTrue(empty.monthlyData.isEmpty())

            val summary = DaySummary(weightEntry.date, weight = weightEntry)
            resultFlow.value = HistoryResult(
                monthlyData = mapOf(weightEntry.date to summary),
                weightTrend = listOf(WeightTrendPoint(weightEntry.date, weightEntry.weight)),
                streak = 1,
                activeDays = 1,
            )
            val updated = awaitItem()
            assertEquals(1, updated.monthlyData.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `weightTrend is populated from use case result`() = runTest {
        val trend = listOf(WeightTrendPoint(weightEntry.date, weightEntry.weight))
        val result = HistoryResult(
            monthlyData = emptyMap(),
            weightTrend = trend,
            streak = 0,
            activeDays = 0,
        )
        val viewModel = buildViewModel(result)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(1, loaded.weightTrend.size)
            assertEquals(weightEntry.date, loaded.weightTrend[0].date)
            assertEquals(weightEntry.weight, loaded.weightTrend[0].weight)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // region streak computation (pure function tests — still valid at domain level)

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

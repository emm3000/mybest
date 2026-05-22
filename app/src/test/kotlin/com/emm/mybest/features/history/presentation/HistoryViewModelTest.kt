package com.emm.mybest.features.history.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.HistoryRecentEntry
import com.emm.mybest.domain.usecase.history.HistoryResult
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

    private val fixedMonth = YearMonthValue(2026, 5)

    private val weightEntry = WeightEntry(
        id = "w1",
        date = LocalDate(2026, 5, 10),
        weight = 78.2f,
        note = null,
    )
    private val photo = ProgressPhoto(
        id = "p1",
        date = LocalDate(2026, 5, 7),
        type = PhotoType.TRUNK,
        photoPath = "/tmp/trunk.jpg",
        createdAt = 1L,
    )

    private fun emptyResult() = HistoryResult(
        monthlyData = emptyMap(),
        monthWeightCount = 0,
        monthPhotoCount = 0,
        recentEntries = emptyList(),
    )

    private fun buildViewModel(
        result: HistoryResult = emptyResult(),
        initialMonth: YearMonthValue = fixedMonth,
    ): HistoryViewModel {
        every { getHistoryUseCase(any()) } returns flowOf(result)
        return HistoryViewModel(getHistoryUseCase, deleteWeightByDate, deletePhoto, initialMonth)
    }

    @Test
    fun `initial state has isLoading true before data arrives`() = runTest {
        every { getHistoryUseCase(any()) } returns flowOf(emptyResult())
        val viewModel = buildViewModel()

        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loaded state with empty data has empty monthlyData`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertTrue(loaded.monthlyData.isEmpty())
            assertNull(loaded.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthWeightCount reflects result from use case`() = runTest {
        val summary = DaySummary(weightEntry.date, weight = weightEntry)
        val result = HistoryResult(
            monthlyData = mapOf(weightEntry.date to summary),
            monthWeightCount = 1,
            monthPhotoCount = 0,
            recentEntries = listOf(
                HistoryRecentEntry(date = weightEntry.date, weight = weightEntry.weight, photoTypes = emptyList()),
            ),
        )
        val viewModel = buildViewModel(result)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(1, loaded.monthWeightCount)
            assertEquals(0, loaded.monthPhotoCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthPhotoCount reflects result from use case`() = runTest {
        val summary = DaySummary(photo.date, photos = listOf(photo))
        val result = HistoryResult(
            monthlyData = mapOf(photo.date to summary),
            monthWeightCount = 0,
            monthPhotoCount = 1,
            recentEntries = listOf(
                HistoryRecentEntry(date = photo.date, weight = null, photoTypes = listOf(PhotoType.TRUNK)),
            ),
        )
        val viewModel = buildViewModel(result)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(0, loaded.monthWeightCount)
            assertEquals(1, loaded.monthPhotoCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `recentEntries are populated from use case result`() = runTest {
        val entry = HistoryRecentEntry(
            date = weightEntry.date,
            weight = weightEntry.weight,
            photoTypes = emptyList(),
        )
        val result = HistoryResult(
            monthlyData = emptyMap(),
            monthWeightCount = 1,
            monthPhotoCount = 0,
            recentEntries = listOf(entry),
        )
        val viewModel = buildViewModel(result)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(1, loaded.recentEntries.size)
            assertEquals(entry, loaded.recentEntries[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDateSelected sets selectedDate in state`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDateSelected(LocalDate(2026, 5, 15)))
            val updated = awaitItem()
            assertEquals(LocalDate(2026, 5, 15), updated.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDateDismiss clears selectedDate`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnDateSelected(LocalDate(2026, 5, 15)))
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
        val newMonth = YearMonthValue(2026, 6)

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
        every { getHistoryUseCase(any()) } returns resultFlow
        val viewModel = HistoryViewModel(getHistoryUseCase, deleteWeightByDate, deletePhoto, fixedMonth)

        viewModel.state.test {
            awaitItem()
            val empty = awaitItem()
            assertTrue(empty.monthlyData.isEmpty())

            val summary = DaySummary(weightEntry.date, weight = weightEntry)
            resultFlow.value = HistoryResult(
                monthlyData = mapOf(weightEntry.date to summary),
                monthWeightCount = 1,
                monthPhotoCount = 0,
                recentEntries = listOf(
                    HistoryRecentEntry(date = weightEntry.date, weight = weightEntry.weight, photoTypes = emptyList()),
                ),
            )
            val updated = awaitItem()
            assertEquals(1, updated.monthlyData.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

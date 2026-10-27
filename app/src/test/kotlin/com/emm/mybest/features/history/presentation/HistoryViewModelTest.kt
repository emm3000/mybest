package com.emm.mybest.features.history.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.history.BitacoraDay
import com.emm.mybest.domain.usecase.history.BitacoraEntry
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.GetBitacoraUseCase
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.HistoryRecentEntry
import com.emm.mybest.domain.usecase.history.HistoryResult
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")

private fun appDayClockAt(dateTime: LocalDateTime) = AppDayClock(FixedClock(dateTime.toInstant(LIMA)), LIMA)

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getHistoryUseCase = mockk<GetHistoryUseCase>(relaxed = true)
    private val getBitacoraUseCase = mockk<GetBitacoraUseCase>()
    private val deleteWeightByDate = mockk<DeleteWeightByDateUseCase>(relaxed = true)
    private val deletePhoto = mockk<DeletePhotoUseCase>(relaxed = true)

    private val mayAppDayClock = appDayClockAt(LocalDateTime(2026, 5, 15, 12, 0))

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

    private fun historyResult(
        summaries: List<DaySummary> = emptyList(),
        recentEntries: List<HistoryRecentEntry> = emptyList(),
    ) = HistoryResult(
        monthlyData = summaries.associateBy { it.date },
        monthWeightCount = summaries.count { it.hasWeight },
        monthPhotoCount = summaries.count { it.hasPhoto },
        monthOutingCount = 0,
        monthWonNightCount = 0,
        recentEntries = recentEntries,
    )

    private fun buildViewModel(
        result: HistoryResult = historyResult(),
        appDayClock: AppDayClock = mayAppDayClock,
        bitacoraDays: List<BitacoraDay> = emptyList(),
    ): HistoryViewModel {
        every { getHistoryUseCase(any()) } returns flowOf(result)
        every { getBitacoraUseCase(any()) } returns flowOf(bitacoraDays)
        return HistoryViewModel(getHistoryUseCase, getBitacoraUseCase, deleteWeightByDate, deletePhoto, appDayClock)
    }

    @Test
    fun `month and today come from the app day before 05_00`() = runTest {
        val viewModel = buildViewModel(appDayClock = appDayClockAt(LocalDateTime(2026, 11, 1, 2, 0)))

        viewModel.state.test {
            val initial = awaitItem()
            assertEquals(LocalDate(2026, 10, 31), initial.today)
            assertEquals(YearMonthValue(2026, 10), initial.currentMonth)
            assertEquals(YearMonthValue(2026, 10), initial.selectedMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `current month and today stay fixed when the selected month changes`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnMonthChange(YearMonthValue(2026, 6)))
            val updated = awaitItem()
            assertEquals(LocalDate(2026, 5, 15), updated.today)
            assertEquals(YearMonthValue(2026, 5), updated.currentMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has isLoading true before data arrives`() = runTest {
        every { getHistoryUseCase(any()) } returns flowOf(historyResult())
        val viewModel = buildViewModel()

        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loaded state with empty data has no day marks`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertTrue(loaded.dayMarks.isEmpty())
            assertNull(loaded.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `monthWeightCount reflects result from use case`() = runTest {
        val summary = DaySummary(weightEntry.date, weight = weightEntry)
        val entry = HistoryRecentEntry(date = weightEntry.date, weight = weightEntry.weight, photoTypes = emptyList())
        val viewModel = buildViewModel(historyResult(listOf(summary), listOf(entry)))

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
        val entry = HistoryRecentEntry(date = photo.date, weight = null, photoTypes = listOf(PhotoType.TRUNK))
        val viewModel = buildViewModel(historyResult(listOf(summary), listOf(entry)))

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
        val viewModel = buildViewModel(historyResult(recentEntries = listOf(entry)))

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(1, loaded.recentEntries.size)
            assertEquals(entry.toRowState(loaded.today), loaded.recentEntries[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the calendar is the initial view and OnViewSelected switches it`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            assertEquals(HistoryView.CALENDAR, awaitItem().selectedView)

            viewModel.onIntent(HistoryIntent.OnViewSelected(HistoryView.BITACORA))
            assertEquals(HistoryView.BITACORA, awaitItem().selectedView)

            viewModel.onIntent(HistoryIntent.OnViewSelected(HistoryView.CALENDAR))
            assertEquals(HistoryView.CALENDAR, awaitItem().selectedView)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `bitacoraDays follow the use case`() = runTest {
        val day = BitacoraDay(LocalDate(2026, 5, 10), listOf(BitacoraEntry.StayedInEntry))
        val viewModel = buildViewModel(bitacoraDays = listOf(day))

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(listOf(day.toState(loaded.today)), loaded.bitacoraDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnMonthChange subscribes the Bitácora to the new month`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()

            viewModel.onIntent(HistoryIntent.OnMonthChange(YearMonthValue(2026, 4)))
            awaitItem()

            verify { getBitacoraUseCase(YearMonthValue(2026, 4)) }
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
        val resultFlow = MutableStateFlow(historyResult())
        every { getHistoryUseCase(any()) } returns resultFlow
        every { getBitacoraUseCase(any()) } returns flowOf(emptyList())
        val viewModel = HistoryViewModel(
            getHistoryUseCase,
            getBitacoraUseCase,
            deleteWeightByDate,
            deletePhoto,
            mayAppDayClock,
        )

        viewModel.state.test {
            awaitItem()
            val empty = awaitItem()
            assertTrue(empty.dayMarks.isEmpty())

            val summary = DaySummary(weightEntry.date, weight = weightEntry)
            resultFlow.value = historyResult(listOf(summary))
            val updated = awaitItem()
            assertEquals(1, updated.dayMarks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

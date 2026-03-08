package com.emm.mybest.features.history.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.DailyHabitRepository
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val weightRepository = mockk<WeightRepository>()
    private val habitRepository = mockk<DailyHabitRepository>()
    private val photoRepository = mockk<PhotoRepository>()

    @Test
    fun `state aggregates data by date`() = runTest {
        val date = LocalDate(2026, 3, 8)
        every { weightRepository.getWeightProgress() } returns flowOf(
            listOf(WeightEntry("w1", date, 77f)),
        )
        every { habitRepository.getAllDailyHabits() } returns flowOf(
            listOf(DailyHabitSummary(date, ateHealthy = true, didExercise = false, notes = null)),
        )
        every { photoRepository.getAllPhotos() } returns flowOf(
            listOf(ProgressPhoto("p1", date = date, type = PhotoType.BODY, photoPath = "/tmp/p1.jpg", createdAt = 1L)),
        )

        val viewModel = HistoryViewModel(weightRepository, habitRepository, photoRepository)
        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            val summary = loaded.monthlyData[date]
            assertEquals(77f, summary?.weight?.weight)
            assertEquals(true, summary?.habit?.ateHealthy)
            assertEquals(1, summary?.photos?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnMonthChange and OnDateSelected update state`() = runTest {
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { habitRepository.getAllDailyHabits() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())

        val viewModel = HistoryViewModel(weightRepository, habitRepository, photoRepository)
        val targetMonth = YearMonthValue(2026, 2)
        val targetDate = LocalDate(2026, 2, 20)

        viewModel.state.test {
            awaitItem() // loading
            awaitItem() // initial
            viewModel.onIntent(HistoryIntent.OnMonthChange(targetMonth))
            assertEquals(targetMonth, awaitItem().selectedMonth)
            viewModel.onIntent(HistoryIntent.OnDateSelected(targetDate))
            assertEquals(targetDate, awaitItem().selectedDate)
            viewModel.onIntent(HistoryIntent.OnDateDismiss)
            assertEquals(null, awaitItem().selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete intents delegate to repositories`() = runTest {
        val date = LocalDate(2026, 3, 8)
        every { weightRepository.getWeightProgress() } returns flowOf(emptyList())
        every { habitRepository.getAllDailyHabits() } returns flowOf(emptyList())
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())
        coEvery { weightRepository.deleteByDate(any()) } returns Unit
        coEvery { habitRepository.deleteByDate(any()) } returns Unit
        coEvery { photoRepository.deletePhoto(any()) } returns Unit

        val viewModel = HistoryViewModel(weightRepository, habitRepository, photoRepository)
        viewModel.onIntent(HistoryIntent.OnDeleteWeight(date))
        viewModel.onIntent(HistoryIntent.OnDeleteHabit(date))
        viewModel.onIntent(HistoryIntent.OnDeletePhoto("p-1"))
        advanceUntilIdle()

        coVerify(exactly = 1) { weightRepository.deleteByDate(date) }
        coVerify(exactly = 1) { habitRepository.deleteByDate(date) }
        coVerify(exactly = 1) { photoRepository.deletePhoto("p-1") }
    }
}

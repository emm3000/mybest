package com.emm.mybest.features.history.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.DailyHabitRepository
import com.emm.mybest.domain.repository.HabitRepository
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
import kotlinx.datetime.DayOfWeek
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
    private val allHabitsRepository = mockk<HabitRepository>()

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
            listOf(
                ProgressPhoto(
                    "p1",
                    habitId = "h-1",
                    date = date,
                    type = PhotoType.BODY,
                    photoPath = "/tmp/p1.jpg",
                    createdAt = 1L,
                ),
            ),
        )
        every { allHabitsRepository.getAllHabits() } returns flowOf(
            listOf(
                Habit(
                    id = "h-1",
                    name = "Entrenar",
                    icon = "FitnessCenter",
                    color = 1,
                    category = "Salud",
                    type = HabitType.BOOLEAN,
                    scheduledDays = setOf(DayOfWeek.MONDAY),
                ),
            ),
        )

        val viewModel = HistoryViewModel(weightRepository, habitRepository, photoRepository, allHabitsRepository)
        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            val summary = loaded.monthlyData[date]
            assertEquals(77f, summary?.weight?.weight)
            assertEquals(true, summary?.habit?.ateHealthy)
            assertEquals(1, summary?.photos?.size)
            assertEquals("Entrenar", summary?.photoHabitNames?.get("p1"))
            assertEquals(1, loaded.monthSummary.activityDays)
            assertEquals(1, loaded.monthSummary.weightDays)
            assertEquals(1, loaded.monthSummary.habitDays)
            assertEquals(1, loaded.monthSummary.photoDays)
            assertEquals(LocalDate(2026, 3, 2), loaded.weekSummary.startDate)
            assertEquals(LocalDate(2026, 3, 8), loaded.weekSummary.endDate)
            assertEquals(1, loaded.weekSummary.activityDays)
            assertEquals(1, loaded.weekSummary.weightDays)
            assertEquals(1, loaded.weekSummary.habitDays)
            assertEquals(1, loaded.weekSummary.photoDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnMonthChange and OnDateSelected update state`() = runTest {
        val firstWeekDate = LocalDate(2026, 2, 3)
        val thirdWeekDate = LocalDate(2026, 2, 20)
        every { weightRepository.getWeightProgress() } returns flowOf(
            listOf(
                WeightEntry("w1", firstWeekDate, 77f),
                WeightEntry("w2", thirdWeekDate, 76f),
            ),
        )
        every { habitRepository.getAllDailyHabits() } returns flowOf(
            listOf(DailyHabitSummary(firstWeekDate, ateHealthy = true, didExercise = false, notes = null)),
        )
        every { photoRepository.getAllPhotos() } returns flowOf(emptyList())
        every { allHabitsRepository.getAllHabits() } returns flowOf(emptyList())

        val viewModel = HistoryViewModel(weightRepository, habitRepository, photoRepository, allHabitsRepository)
        val targetMonth = YearMonthValue(2026, 2)
        val targetDate = thirdWeekDate

        viewModel.state.test {
            awaitItem() // loading
            awaitItem() // initial
            viewModel.onIntent(HistoryIntent.OnMonthChange(targetMonth))
            val monthState = awaitItem()
            assertEquals(targetMonth, monthState.selectedMonth)
            assertEquals(LocalDate(2026, 2, 16), monthState.weekSummary.startDate)
            assertEquals(LocalDate(2026, 2, 22), monthState.weekSummary.endDate)
            viewModel.onIntent(HistoryIntent.OnDateSelected(targetDate))
            val selectedState = awaitItem()
            assertEquals(targetDate, selectedState.selectedDate)
            assertEquals(LocalDate(2026, 2, 16), selectedState.weekSummary.startDate)
            assertEquals(LocalDate(2026, 2, 22), selectedState.weekSummary.endDate)
            assertEquals(1, selectedState.weekSummary.activityDays)
            assertEquals(1, selectedState.weekSummary.weightDays)
            assertEquals(0, selectedState.weekSummary.habitDays)
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
        every { allHabitsRepository.getAllHabits() } returns flowOf(emptyList())
        coEvery { weightRepository.deleteByDate(any()) } returns Unit
        coEvery { habitRepository.deleteByDate(any()) } returns Unit
        coEvery { photoRepository.deletePhoto(any()) } returns Unit

        val viewModel = HistoryViewModel(weightRepository, habitRepository, photoRepository, allHabitsRepository)
        viewModel.onIntent(HistoryIntent.OnDeleteWeight(date))
        viewModel.onIntent(HistoryIntent.OnDeleteHabit(date))
        viewModel.onIntent(HistoryIntent.OnDeletePhoto("p-1"))
        advanceUntilIdle()

        coVerify(exactly = 1) { weightRepository.deleteByDate(date) }
        coVerify(exactly = 1) { habitRepository.deleteByDate(date) }
        coVerify(exactly = 1) { photoRepository.deletePhoto("p-1") }
    }
}

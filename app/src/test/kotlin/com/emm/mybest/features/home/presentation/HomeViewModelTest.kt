package com.emm.mybest.features.home.presentation

import app.cash.turbine.test
import com.emm.mybest.core.navigation.Screen
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.models.HomeSummary
import com.emm.mybest.domain.usecase.GetHomeSummaryUseCase
import com.emm.mybest.domain.usecase.ToggleHabitUseCase
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
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getHomeSummaryUseCase = mockk<GetHomeSummaryUseCase>()
    private val toggleHabitUseCase = mockk<ToggleHabitUseCase>()

    @Test
    fun `state maps summary data from use case`() = runTest {
        val habits = listOf(
            HabitWithRecord(
                habit = Habit(
                    id = "h-1",
                    name = "Beber agua",
                    icon = "WaterDrop",
                    color = 1,
                    category = "Salud",
                    type = HabitType.BOOLEAN,
                    scheduledDays = setOf(DayOfWeek.MONDAY),
                ),
                record = null,
            ),
        )
        every { getHomeSummaryUseCase.invoke() } returns flowOf(
            HomeSummary(
                dailyHabits = habits,
                latestWeight = 77f,
                totalWeightLost = 3f,
                totalPhotos = 8,
            ),
        )

        val viewModel = HomeViewModel(getHomeSummaryUseCase, toggleHabitUseCase)
        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            val state = awaitItem()
            assertEquals(habits, state.dailyHabits)
            assertEquals(77f, state.lastWeight)
            assertEquals(3f, state.totalWeightLost)
            assertEquals(8, state.totalPhotos)
            assertEquals(false, state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnAddWeightClick emits navigate effect`() = runTest {
        every { getHomeSummaryUseCase.invoke() } returns flowOf(
            HomeSummary(emptyList(), null, 0f, 0),
        )
        val viewModel = HomeViewModel(getHomeSummaryUseCase, toggleHabitUseCase)

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.OnAddWeightClick)
            assertEquals(HomeEffect.Navigate(Screen.AddWeight), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigation intents emit expected routes`() = runTest {
        every { getHomeSummaryUseCase.invoke() } returns flowOf(
            HomeSummary(emptyList(), null, 0f, 0),
        )
        val viewModel = HomeViewModel(getHomeSummaryUseCase, toggleHabitUseCase)

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.OnAddHabitClick)
            assertEquals(HomeEffect.Navigate(Screen.AddHabit), awaitItem())
            viewModel.onIntent(HomeIntent.OnAddPhotoClick)
            assertEquals(HomeEffect.Navigate(Screen.AddPhoto), awaitItem())
            viewModel.onIntent(HomeIntent.OnViewHistoryClick)
            assertEquals(HomeEffect.Navigate(Screen.History), awaitItem())
            viewModel.onIntent(HomeIntent.OnViewInsightsClick)
            assertEquals(HomeEffect.Navigate(Screen.Insights), awaitItem())
            viewModel.onIntent(HomeIntent.OnViewTimelineClick)
            assertEquals(HomeEffect.Navigate(Screen.Timeline), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleHabit emits error effect when use case throws`() = runTest {
        val habitWithRecord = HabitWithRecord(
            habit = Habit(
                id = "h-1",
                name = "Beber agua",
                icon = "WaterDrop",
                color = 1,
                category = "Salud",
                type = HabitType.BOOLEAN,
                scheduledDays = setOf(DayOfWeek.MONDAY),
            ),
            record = null,
        )
        every { getHomeSummaryUseCase.invoke() } returns flowOf(
            HomeSummary(emptyList(), null, 0f, 0),
        )
        coEvery { toggleHabitUseCase.invoke(any(), any()) } throws IllegalStateException("boom")
        val viewModel = HomeViewModel(getHomeSummaryUseCase, toggleHabitUseCase)

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.ToggleHabit(habitWithRecord))
            assertEquals(HomeEffect.ShowError("boom"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { toggleHabitUseCase.invoke(any(), any()) }
    }

    @Test
    fun `ToggleHabit success delegates without error effect`() = runTest {
        val habitWithRecord = HabitWithRecord(
            habit = Habit(
                id = "h-2",
                name = "Leer",
                icon = "MenuBook",
                color = 2,
                category = "Mind",
                type = HabitType.BOOLEAN,
                scheduledDays = setOf(DayOfWeek.TUESDAY),
            ),
            record = null,
        )
        every { getHomeSummaryUseCase.invoke() } returns flowOf(
            HomeSummary(emptyList(), null, 0f, 0),
        )
        coEvery { toggleHabitUseCase.invoke(any(), any()) } returns Unit
        val viewModel = HomeViewModel(getHomeSummaryUseCase, toggleHabitUseCase)

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.ToggleHabit(habitWithRecord))
            assertEquals(HomeEffect.ShowSuccess("Hábito completado"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()
        coVerify(exactly = 1) { toggleHabitUseCase.invoke(habitWithRecord, any()) }
    }

    @Test
    fun `ToggleHabit success emits pending message when completed habit is unchecked`() = runTest {
        val habitWithRecord = HabitWithRecord(
            habit = Habit(
                id = "h-3",
                name = "Correr",
                icon = "DirectionsRun",
                color = 3,
                category = "Deporte",
                type = HabitType.BOOLEAN,
                scheduledDays = setOf(DayOfWeek.WEDNESDAY),
            ),
            record = HabitRecord(
                id = "r-1",
                habitId = "h-3",
                date = LocalDate(2026, 3, 12),
                value = 1f,
                isCompleted = true,
            ),
        )
        every { getHomeSummaryUseCase.invoke() } returns flowOf(
            HomeSummary(emptyList(), null, 0f, 0),
        )
        coEvery { toggleHabitUseCase.invoke(any(), any()) } returns Unit
        val viewModel = HomeViewModel(getHomeSummaryUseCase, toggleHabitUseCase)

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.ToggleHabit(habitWithRecord))
            assertEquals(HomeEffect.ShowSuccess("Hábito marcado como pendiente"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()
        coVerify(exactly = 1) { toggleHabitUseCase.invoke(habitWithRecord, any()) }
    }
}

package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailyCompliance
import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.DailySlotTimes
import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.models.WeeklyExercisePlan
import com.emm.mybest.domain.models.WeeklyMealPlan
import com.emm.mybest.domain.usecase.compliance.GetCompletionStreakUseCase
import com.emm.mybest.domain.usecase.compliance.ObserveDailyComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleExerciseComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleMealComplianceUseCase
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
import com.emm.mybest.domain.usecase.preferences.ObserveDailySlotTimesUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private val observeCompliance: ObserveDailyComplianceUseCase = mockk()
    private val toggleMeal: ToggleMealComplianceUseCase = mockk(relaxed = true)
    private val toggleExercise: ToggleExerciseComplianceUseCase = mockk(relaxed = true)
    private val getMealPlan: GetWeeklyMealPlanUseCase = mockk()
    private val getExercisePlan: GetWeeklyExercisePlanUseCase = mockk()
    private val getCompletionStreak: GetCompletionStreakUseCase = mockk()
    private val observeDailySlotTimes: ObserveDailySlotTimesUseCase = mockk()

    private fun emptyCompliance() = DailyCompliance(
        date = FIXED_DATE,
        mealsDone = MealType.entries.associateWith { false },
        exerciseDone = false,
    )

    private fun buildViewModel(
        mealPlan: WeeklyMealPlan = WeeklyMealPlan(emptyList()),
        exercisePlan: WeeklyExercisePlan = WeeklyExercisePlan(emptyList()),
        compliance: DailyCompliance = emptyCompliance(),
        streak: Int = 0,
    ): HomeViewModel {
        every { getMealPlan() } returns flowOf(mealPlan)
        every { getExercisePlan() } returns flowOf(exercisePlan)
        every { observeCompliance(any()) } returns flowOf(compliance)
        every { getCompletionStreak(any()) } returns flowOf(streak)
        every { observeDailySlotTimes() } returns flowOf(DailySlotTimes(emptyMap()))
        return HomeViewModel(
            observeDailyCompliance = observeCompliance,
            toggleUseCases = HomeToggleUseCases(toggleMeal, toggleExercise),
            getMealPlan = getMealPlan,
            getExercisePlan = getExercisePlan,
            getCompletionStreak = getCompletionStreak,
            observeDailySlotTimes = observeDailySlotTimes,
            clock = FIXED_CLOCK,
        )
    }

    @Test
    fun `empty plan and no compliance yields 5 plan rows all done-false and zero ratio`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(5, state.planRows.size)
        state.planRows.forEach { row ->
            assertFalse(row.done)
            assertEquals("", row.description)
        }
        assertEquals(0f, state.completionRatio, 0.001f)
        assertEquals(0, state.completedCount)
    }

    @Test
    fun `plan with descriptions and compliance 2 meals plus exercise gives correct state`() = runTest {
        val dow = DayOfWeek.SUNDAY
        val mealPlan = WeeklyMealPlan(
            listOf(
                MealPlanEntry(dow, MealType.BREAKFAST, "Avena con frutas"),
                MealPlanEntry(dow, MealType.LUNCH, "Arroz con pollo"),
                MealPlanEntry(dow, MealType.DINNER, "Sopa de verduras"),
                MealPlanEntry(dow, MealType.SNACK, "Manzana"),
            ),
        )
        val exercisePlan = WeeklyExercisePlan(
            listOf(ExercisePlanEntry(dow, "Cardio 30 min")),
        )
        val compliance = DailyCompliance(
            date = FIXED_DATE,
            mealsDone = mapOf(
                MealType.BREAKFAST to true,
                MealType.LUNCH to true,
                MealType.DINNER to false,
                MealType.SNACK to false,
            ),
            exerciseDone = true,
        )
        val viewModel = buildViewModel(mealPlan, exercisePlan, compliance)
        advanceUntilIdle()

        val state = viewModel.state.value
        val breakfastRow = state.planRows.first { it.slot == DailySlot.BREAKFAST }
        val lunchRow = state.planRows.first { it.slot == DailySlot.LUNCH }
        val dinnerRow = state.planRows.first { it.slot == DailySlot.DINNER }
        val exerciseRow = state.planRows.first { it.slot == DailySlot.EXERCISE }
        assertEquals("Avena con frutas", breakfastRow.description)
        assertEquals(true, breakfastRow.done)
        assertEquals(true, lunchRow.done)
        assertEquals(false, dinnerRow.done)
        assertEquals("Cardio 30 min", exerciseRow.description)
        assertEquals(true, exerciseRow.done)
        assertEquals(3, state.completedCount)
        assertEquals(3f / 5f, state.completionRatio, 0.001f)
    }

    @Test
    fun `ToggleSlot BREAKFAST true calls toggleMeal with correct args`() = runTest {
        coEvery { toggleMeal(any(), any(), any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.ToggleSlot(DailySlot.BREAKFAST, true))
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleMeal(FIXED_DATE, MealType.BREAKFAST, true) }
    }

    @Test
    fun `ToggleSlot LUNCH false calls toggleMeal with correct args`() = runTest {
        coEvery { toggleMeal(any(), any(), any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.ToggleSlot(DailySlot.LUNCH, false))
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleMeal(FIXED_DATE, MealType.LUNCH, false) }
    }

    @Test
    fun `ToggleSlot EXERCISE true calls toggleExercise with correct args`() = runTest {
        coEvery { toggleExercise(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.ToggleSlot(DailySlot.EXERCISE, true))
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleExercise(FIXED_DATE, true) }
    }

    @Test
    fun `streak from use case propagates to state streakDays`() = runTest {
        val viewModel = buildViewModel(streak = 7)
        advanceUntilIdle()
        assertEquals(7, viewModel.state.value.streakDays)
    }
}

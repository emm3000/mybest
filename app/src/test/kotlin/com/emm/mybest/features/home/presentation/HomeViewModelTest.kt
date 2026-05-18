package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.models.DailyCompliance
import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.models.WeeklyExercisePlan
import com.emm.mybest.domain.models.WeeklyMealPlan
import com.emm.mybest.domain.usecase.compliance.ObserveDailyComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleExerciseComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleMealComplianceUseCase
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

// Fixed clock: Sunday 2026-05-17 noon UTC — stays in Sunday for any reasonable TZ
private val FIXED_DATE = LocalDate(2026, 5, 17)
private val FIXED_CLOCK = object : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(1_779_019_200L) // 2026-05-17T12:00:00Z
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeCompliance: ObserveDailyComplianceUseCase = mockk()
    private val toggleMeal: ToggleMealComplianceUseCase = mockk(relaxed = true)
    private val toggleExercise: ToggleExerciseComplianceUseCase = mockk(relaxed = true)
    private val getMealPlan: GetWeeklyMealPlanUseCase = mockk()
    private val getExercisePlan: GetWeeklyExercisePlanUseCase = mockk()

    private fun emptyCompliance() = DailyCompliance(
        date = FIXED_DATE,
        mealsDone = MealType.entries.associateWith { false },
        exerciseDone = false,
    )

    private fun buildViewModel(
        mealPlan: WeeklyMealPlan = WeeklyMealPlan(emptyList()),
        exercisePlan: WeeklyExercisePlan = WeeklyExercisePlan(emptyList()),
        compliance: DailyCompliance = emptyCompliance(),
    ): HomeViewModel {
        every { getMealPlan() } returns flowOf(mealPlan)
        every { getExercisePlan() } returns flowOf(exercisePlan)
        every { observeCompliance(any()) } returns flowOf(compliance)
        return HomeViewModel(
            observeDailyCompliance = observeCompliance,
            toggleMeal = toggleMeal,
            toggleExercise = toggleExercise,
            getMealPlan = getMealPlan,
            getExercisePlan = getExercisePlan,
            clock = FIXED_CLOCK,
        )
    }

    @Test
    fun `empty plan and no compliance yields 4 meal rows all done-false and zero ratio`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(4, state.mealRows.size)
        state.mealRows.forEach { row ->
            assertFalse(row.done)
            assertEquals("", row.description)
        }
        assertEquals("", state.exerciseRoutine)
        assertFalse(state.exerciseDone)
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
        assertEquals("Avena con frutas", state.mealRows.first { it.type == MealType.BREAKFAST }.description)
        assertEquals(true, state.mealRows.first { it.type == MealType.BREAKFAST }.done)
        assertEquals(true, state.mealRows.first { it.type == MealType.LUNCH }.done)
        assertEquals(false, state.mealRows.first { it.type == MealType.DINNER }.done)
        assertEquals("Cardio 30 min", state.exerciseRoutine)
        assertEquals(true, state.exerciseDone)
        assertEquals(3, state.completedCount)
        assertEquals(3f / 5f, state.completionRatio, 0.001f)
    }

    @Test
    fun `ToggleMeal BREAKFAST true calls toggleMeal with correct args`() = runTest {
        coEvery { toggleMeal(any(), any(), any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.handle(HomeIntent.ToggleMeal(MealType.BREAKFAST, true))
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleMeal(FIXED_DATE, MealType.BREAKFAST, true) }
    }

    @Test
    fun `ToggleMeal LUNCH false calls toggleMeal with correct args`() = runTest {
        coEvery { toggleMeal(any(), any(), any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.handle(HomeIntent.ToggleMeal(MealType.LUNCH, false))
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleMeal(FIXED_DATE, MealType.LUNCH, false) }
    }

    @Test
    fun `ToggleExercise true calls toggleExercise with correct args`() = runTest {
        coEvery { toggleExercise(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.handle(HomeIntent.ToggleExercise(true))
        advanceUntilIdle()

        coVerify(exactly = 1) { toggleExercise(FIXED_DATE, true) }
    }
}

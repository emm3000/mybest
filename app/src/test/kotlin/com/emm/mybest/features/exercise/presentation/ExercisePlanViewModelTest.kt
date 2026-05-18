package com.emm.mybest.features.exercise.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.domain.models.WeeklyExercisePlan
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
import com.emm.mybest.domain.usecase.exercise.UpsertExerciseRoutineUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisePlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPlan: GetWeeklyExercisePlanUseCase = mockk()
    private val upsertRoutine: UpsertExerciseRoutineUseCase = mockk(relaxed = true)

    private fun buildViewModel(plan: WeeklyExercisePlan = WeeklyExercisePlan(emptyList())): ExercisePlanViewModel {
        every { getPlan() } returns flowOf(plan)
        return ExercisePlanViewModel(getPlan, upsertRoutine)
    }

    @Test
    fun `initial loading then empty plan yields 7 days with empty routines`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(7, state.routines.size)
        DayOfWeek.entries.forEach { day ->
            assertEquals("", state.routines[day])
        }
    }

    @Test
    fun `existing plan entries are loaded correctly into state`() = runTest {
        val plan = WeeklyExercisePlan(
            listOf(
                ExercisePlanEntry(DayOfWeek.MONDAY, "Pecho + tríceps"),
                ExercisePlanEntry(DayOfWeek.WEDNESDAY, "Espalda + bíceps"),
                ExercisePlanEntry(DayOfWeek.FRIDAY, "Pierna completa"),
            ),
        )
        val viewModel = buildViewModel(plan)
        advanceUntilIdle()

        val routines = viewModel.state.value.routines
        assertEquals("Pecho + tríceps", routines[DayOfWeek.MONDAY])
        assertEquals("Espalda + bíceps", routines[DayOfWeek.WEDNESDAY])
        assertEquals("Pierna completa", routines[DayOfWeek.FRIDAY])
        assertEquals("", routines[DayOfWeek.TUESDAY])
        assertEquals("", routines[DayOfWeek.SUNDAY])
    }

    @Test
    fun `StartEdit with existing routine sets editing draftRoutine to current value`() = runTest {
        val plan = WeeklyExercisePlan(
            listOf(ExercisePlanEntry(DayOfWeek.MONDAY, "Cardio 30 min")),
        )
        val viewModel = buildViewModel(plan)
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.MONDAY))

        val editing = viewModel.state.value.editing
        assertNotNull(editing)
        assertEquals(DayOfWeek.MONDAY, editing!!.day)
        assertEquals("Cardio 30 min", editing.draftRoutine)
    }

    @Test
    fun `StartEdit on empty day sets empty draftRoutine`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.TUESDAY))

        val editing = viewModel.state.value.editing
        assertNotNull(editing)
        assertEquals(DayOfWeek.TUESDAY, editing!!.day)
        assertEquals("", editing.draftRoutine)
    }

    @Test
    fun `UpdateDraft updates draftRoutine in editing`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.WEDNESDAY))
        viewModel.onIntent(ExercisePlanIntent.UpdateDraft("Hombros + trapecio"))

        assertEquals("Hombros + trapecio", viewModel.state.value.editing?.draftRoutine)
    }

    @Test
    fun `SaveRoutine calls upsertRoutine with trimmed routine and emits DismissSheet`() = runTest {
        val capturedEntry = slot<ExercisePlanEntry>()
        coEvery { upsertRoutine(capture(capturedEntry)) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.THURSDAY))
        viewModel.onIntent(ExercisePlanIntent.UpdateDraft("  Sentadillas y peso muerto  "))

        viewModel.effects.test {
            viewModel.onIntent(ExercisePlanIntent.SaveRoutine)
            advanceUntilIdle()

            assertEquals(ExercisePlanEffect.DismissSheet, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { upsertRoutine(any()) }
        assertEquals(DayOfWeek.THURSDAY, capturedEntry.captured.dayOfWeek)
        assertEquals("Sentadillas y peso muerto", capturedEntry.captured.routine)
        assertNull(viewModel.state.value.editing)
    }

    @Test
    fun `SaveRoutine without active editing does nothing`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(ExercisePlanIntent.SaveRoutine)
            advanceUntilIdle()

            expectNoEvents()
        }

        coVerify(exactly = 0) { upsertRoutine(any()) }
    }

    @Test
    fun `CancelEdit clears editing without saving`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.FRIDAY))
        viewModel.onIntent(ExercisePlanIntent.UpdateDraft("Abdominales"))
        viewModel.onIntent(ExercisePlanIntent.CancelEdit)

        assertNull(viewModel.state.value.editing)
        coVerify(exactly = 0) { upsertRoutine(any()) }
    }

    @Test
    fun `loading is true initially and false after plan is received`() = runTest {
        val viewModel = buildViewModel()

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()
        assertEquals(false, viewModel.state.value.isLoading)
    }
}

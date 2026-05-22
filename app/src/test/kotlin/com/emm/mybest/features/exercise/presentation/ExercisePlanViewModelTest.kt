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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

private val FIXED_DATE = LocalDate(2026, 5, 21)

private object FixedClock : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(1_779_364_800L)
}

class ExercisePlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPlan: GetWeeklyExercisePlanUseCase = mockk()
    private val upsertRoutine: UpsertExerciseRoutineUseCase = mockk(relaxed = true)

    private fun buildViewModel(
        plan: WeeklyExercisePlan = WeeklyExercisePlan(emptyList()),
    ): ExercisePlanViewModel {
        every { getPlan() } returns flowOf(plan)
        return ExercisePlanViewModel(getPlan, upsertRoutine, FixedClock)
    }

    @Test
    fun `initial state has today from clock`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(FIXED_DATE, viewModel.state.value.today)
    }

    @Test
    fun `initial loading then empty plan yields 7 days with blank entries`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(7, state.entries.size)
        DayOfWeek.entries.forEach { day ->
            val entry = state.entries[day]
            assertNotNull(entry)
            assertEquals("", entry!!.name)
            assertEquals("", entry.detail)
            assertEquals("", entry.volume)
        }
    }

    @Test
    fun `existing plan entries are loaded correctly into state`() = runTest {
        val plan = WeeklyExercisePlan(
            listOf(
                ExercisePlanEntry(DayOfWeek.MONDAY, "Pecho", "Pecho + tríceps", "4x12"),
                ExercisePlanEntry(DayOfWeek.WEDNESDAY, "Espalda", "Espalda + bíceps", "3x10"),
            ),
        )
        val viewModel = buildViewModel(plan)
        advanceUntilIdle()

        val entries = viewModel.state.value.entries
        assertEquals("Pecho", entries[DayOfWeek.MONDAY]?.name)
        assertEquals("Pecho + tríceps", entries[DayOfWeek.MONDAY]?.detail)
        assertEquals("4x12", entries[DayOfWeek.MONDAY]?.volume)
        assertEquals("Espalda", entries[DayOfWeek.WEDNESDAY]?.name)
        assertEquals("", entries[DayOfWeek.TUESDAY]?.name)
    }

    @Test
    fun `StartEdit with existing entry sets editing draft to current values`() = runTest {
        val plan = WeeklyExercisePlan(
            listOf(ExercisePlanEntry(DayOfWeek.MONDAY, "Cardio", "30 min", "1 sesión")),
        )
        val viewModel = buildViewModel(plan)
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.MONDAY))

        val editing = viewModel.state.value.editing
        assertNotNull(editing)
        assertEquals(DayOfWeek.MONDAY, editing!!.day)
        assertEquals("Cardio", editing.name)
        assertEquals("30 min", editing.detail)
        assertEquals("1 sesión", editing.volume)
    }

    @Test
    fun `StartEdit on empty day sets blank draft`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.TUESDAY))

        val editing = viewModel.state.value.editing
        assertNotNull(editing)
        assertEquals(DayOfWeek.TUESDAY, editing!!.day)
        assertEquals("", editing.name)
        assertEquals("", editing.detail)
        assertEquals("", editing.volume)
    }

    @Test
    fun `UpdateName updates name in editing draft`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.WEDNESDAY))
        viewModel.onIntent(ExercisePlanIntent.UpdateName("Push"))

        assertEquals("Push", viewModel.state.value.editing?.name)
    }

    @Test
    fun `UpdateDetail updates detail in editing draft`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.THURSDAY))
        viewModel.onIntent(ExercisePlanIntent.UpdateDetail("Hombros + trapecio"))

        assertEquals("Hombros + trapecio", viewModel.state.value.editing?.detail)
    }

    @Test
    fun `UpdateVolume updates volume in editing draft`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.FRIDAY))
        viewModel.onIntent(ExercisePlanIntent.UpdateVolume("5x5"))

        assertEquals("5x5", viewModel.state.value.editing?.volume)
    }

    @Test
    fun `SaveRoutine calls upsert with trimmed fields and emits DismissSheet`() = runTest {
        val capturedEntry = slot<ExercisePlanEntry>()
        coEvery { upsertRoutine(capture(capturedEntry)) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.THURSDAY))
        viewModel.onIntent(ExercisePlanIntent.UpdateName("  Piernas  "))
        viewModel.onIntent(ExercisePlanIntent.UpdateDetail("  Sentadillas  "))
        viewModel.onIntent(ExercisePlanIntent.UpdateVolume("  4x10  "))

        viewModel.effects.test {
            viewModel.onIntent(ExercisePlanIntent.SaveRoutine)
            advanceUntilIdle()

            assertEquals(ExercisePlanEffect.DismissSheet, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { upsertRoutine(any()) }
        assertEquals(DayOfWeek.THURSDAY, capturedEntry.captured.dayOfWeek)
        assertEquals("Piernas", capturedEntry.captured.name)
        assertEquals("Sentadillas", capturedEntry.captured.detail)
        assertEquals("4x10", capturedEntry.captured.volume)
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
        viewModel.onIntent(ExercisePlanIntent.UpdateName("Abdominales"))
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

    @Test
    fun `name clamped to MAX_NAME_LENGTH`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ExercisePlanIntent.StartEdit(DayOfWeek.SATURDAY))
        val longName = "A".repeat(100)
        viewModel.onIntent(ExercisePlanIntent.UpdateName(longName))

        assertEquals(40, viewModel.state.value.editing?.name?.length)
    }
}

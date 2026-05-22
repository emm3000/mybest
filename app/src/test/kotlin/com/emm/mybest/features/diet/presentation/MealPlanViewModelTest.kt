package com.emm.mybest.features.diet.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.models.WeeklyMealPlan
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.diet.UpsertMealUseCase
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MealPlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPlan: GetWeeklyMealPlanUseCase = mockk()
    private val upsertMeal: UpsertMealUseCase = mockk(relaxed = true)

    private fun buildViewModel(plan: WeeklyMealPlan = WeeklyMealPlan(emptyList())): MealPlanViewModel {
        every { getPlan() } returns flowOf(plan)
        return MealPlanViewModel(getPlan, upsertMeal)
    }

    @Test
    fun `initial loading then empty plan yields 7 days with 4 empty entries each`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(7, state.entries.size)
        DayOfWeek.entries.forEach { day ->
            val dayEntries = state.entries[day]
            assertNotNull(dayEntries)
            assertEquals(4, dayEntries!!.size)
            MealType.entries.forEach { type ->
                assertEquals("", dayEntries[type])
            }
        }
    }

    @Test
    fun `StartEdit sets editing with current draft from state`() = runTest {
        val plan = WeeklyMealPlan(
            listOf(MealPlanEntry(DayOfWeek.MONDAY, MealType.BREAKFAST, "Avena")),
        )
        val viewModel = buildViewModel(plan)
        advanceUntilIdle()

        viewModel.onIntent(MealPlanIntent.StartEdit(DayOfWeek.MONDAY, MealType.BREAKFAST))

        val editing = viewModel.state.value.editing
        assertNotNull(editing)
        assertEquals(DayOfWeek.MONDAY, editing!!.day)
        assertEquals(MealType.BREAKFAST, editing.type)
        assertEquals("Avena", editing.description)
    }

    @Test
    fun `StartEdit on empty entry sets empty draft`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MealPlanIntent.StartEdit(DayOfWeek.TUESDAY, MealType.LUNCH))

        val editing = viewModel.state.value.editing
        assertNotNull(editing)
        assertEquals("", editing!!.description)
    }

    @Test
    fun `UpdateDraft updates description in editing`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MealPlanIntent.StartEdit(DayOfWeek.WEDNESDAY, MealType.DINNER))
        viewModel.onIntent(MealPlanIntent.UpdateDraft("Pasta con pollo"))

        assertEquals("Pasta con pollo", viewModel.state.value.editing?.description)
    }

    @Test
    fun `CancelEdit clears editing without saving`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MealPlanIntent.StartEdit(DayOfWeek.FRIDAY, MealType.SNACK))
        viewModel.onIntent(MealPlanIntent.UpdateDraft("Manzana"))
        viewModel.onIntent(MealPlanIntent.CancelEdit)

        assertNull(viewModel.state.value.editing)
    }

    @Test
    fun `SaveMeal calls upsertMeal with trimmed description and emits DismissSheet`() = runTest {
        val capturedEntry = slot<MealPlanEntry>()
        coEvery { upsertMeal(capture(capturedEntry)) } returns Unit
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onIntent(MealPlanIntent.StartEdit(DayOfWeek.THURSDAY, MealType.LUNCH))
        viewModel.onIntent(MealPlanIntent.UpdateDraft("  Pollo a la plancha  "))

        viewModel.effects.test {
            viewModel.onIntent(MealPlanIntent.SaveMeal)
            advanceUntilIdle()

            assertEquals(MealPlanEffect.DismissSheet, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { upsertMeal(any()) }
        assertEquals(DayOfWeek.THURSDAY, capturedEntry.captured.dayOfWeek)
        assertEquals(MealType.LUNCH, capturedEntry.captured.mealType)
        assertEquals("Pollo a la plancha", capturedEntry.captured.description)
        assertNull(viewModel.state.value.editing)
    }

    @Test
    fun `SaveMeal without active editing does nothing`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(MealPlanIntent.SaveMeal)
            advanceUntilIdle()

            expectNoEvents()
        }

        coVerify(exactly = 0) { upsertMeal(any()) }
    }

    @Test
    fun `existing plan entries are loaded correctly into state`() = runTest {
        val plan = WeeklyMealPlan(
            listOf(
                MealPlanEntry(DayOfWeek.MONDAY, MealType.BREAKFAST, "Cafe"),
                MealPlanEntry(DayOfWeek.MONDAY, MealType.LUNCH, "Arroz"),
                MealPlanEntry(DayOfWeek.SUNDAY, MealType.DINNER, "Sopa"),
            ),
        )
        val viewModel = buildViewModel(plan)
        advanceUntilIdle()

        val entries = viewModel.state.value.entries
        assertEquals("Cafe", entries[DayOfWeek.MONDAY]?.get(MealType.BREAKFAST))
        assertEquals("Arroz", entries[DayOfWeek.MONDAY]?.get(MealType.LUNCH))
        assertEquals("", entries[DayOfWeek.MONDAY]?.get(MealType.DINNER))
        assertEquals("Sopa", entries[DayOfWeek.SUNDAY]?.get(MealType.DINNER))
    }

    @Test
    fun `loading is true initially and false after plan is received`() = runTest {
        val viewModel = buildViewModel()

        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()
        assertEquals(false, viewModel.state.value.isLoading)
    }
}

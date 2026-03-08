package com.emm.mybest.features.habit.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.usecase.CreateHabitUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddHabitViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createHabitUseCase: CreateHabitUseCase = mockk(relaxed = true)
    private lateinit var viewModel: AddHabitViewModel

    @Before
    fun setup() {
        viewModel = AddHabitViewModel(createHabitUseCase)
    }

    @Test
    fun `initial state has expected defaults`() {
        val state = viewModel.state.value

        assertEquals(1, state.step)
        assertEquals("", state.name)
        assertEquals(HabitType.BOOLEAN, state.type)
        assertEquals(DayOfWeek.entries.toSet(), state.scheduledDays)
        assertEquals(false, state.isLoading)
        assertNull(state.nameError)
        assertNull(state.goalError)
    }

    @Test
    fun `OnNextStep moves to step 2 when name is valid`() {
        viewModel.onIntent(AddHabitIntent.OnNameChange("Entrenar"))

        viewModel.onIntent(AddHabitIntent.OnNextStep)

        assertEquals(2, viewModel.state.value.step)
        assertNull(viewModel.state.value.nameError)
    }

    @Test
    fun `OnNextStep keeps step 1 and sets error when name is invalid`() {
        viewModel.onIntent(AddHabitIntent.OnNameChange(""))

        viewModel.onIntent(AddHabitIntent.OnNextStep)

        assertEquals(1, viewModel.state.value.step)
        assertEquals("El nombre no puede estar vacío", viewModel.state.value.nameError)
    }

    @Test
    fun `OnSaveClick success emits NavigateBack and calls use case`() = runTest {
        val capturedHabit = slot<Habit>()
        coEvery { createHabitUseCase.invoke(capture(capturedHabit)) } returns Unit

        viewModel.onIntent(AddHabitIntent.OnNameChange("Leer"))
        viewModel.onIntent(AddHabitIntent.OnTypeChange(HabitType.BOOLEAN))

        viewModel.effect.test {
            viewModel.onIntent(AddHabitIntent.OnSaveClick)
            advanceUntilIdle()

            assertEquals(AddHabitEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { createHabitUseCase.invoke(any()) }
        assertEquals("Leer", capturedHabit.captured.name)
        assertEquals(HabitType.BOOLEAN, capturedHabit.captured.type)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `OnSaveClick error emits ShowError`() = runTest {
        coEvery { createHabitUseCase.invoke(any()) } throws IllegalStateException("boom")

        viewModel.effect.test {
            viewModel.onIntent(AddHabitIntent.OnSaveClick)
            advanceUntilIdle()

            val effect = awaitItem()
            assert(effect is AddHabitEffect.ShowError)
            assertEquals("Error al guardar: boom", (effect as AddHabitEffect.ShowError).message)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(false, viewModel.state.value.isLoading)
    }
}

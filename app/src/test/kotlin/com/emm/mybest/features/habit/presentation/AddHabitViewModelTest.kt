package com.emm.mybest.features.habit.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.CreateHabitUseCase
import com.emm.mybest.domain.usecase.GetHabitByIdUseCase
import com.emm.mybest.domain.usecase.UpdateHabitUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddHabitViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val createHabitUseCase: CreateHabitUseCase = mockk(relaxed = true)
    private val getHabitByIdUseCase: GetHabitByIdUseCase = mockk(relaxed = true)
    private val updateHabitUseCase: UpdateHabitUseCase = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private lateinit var viewModel: AddHabitViewModel

    @Before
    fun setup() {
        every { userPreferencesRepository.defaultReminderTime } returns flowOf(Pair(20, 0))
        viewModel = AddHabitViewModel(
            createHabitUseCase,
            getHabitByIdUseCase,
            updateHabitUseCase,
            userPreferencesRepository,
        )
    }

    @Test
    fun `initial state has expected defaults`() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.value

        assertEquals(1, state.step)
        assertEquals("", state.name)
        assertEquals(HabitType.BOOLEAN, state.type)
        assertEquals(DayOfWeek.entries.toSet(), state.scheduledDays)
        assertEquals(false, state.isLoading)
        assertNull(state.nameError)
        assertNull(state.goalError)
        assertNull(state.unitError)
        assertNull(state.scheduledDaysError)
        assertTrue(state.reminderEnabled)
        assertEquals(20, state.reminderHour)
        assertEquals(0, state.reminderMinute)
        assertFalse(state.showTimePicker)
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
    fun `OnNextStep from step 2 moves to step 3 when goal is valid`() {
        viewModel.onIntent(AddHabitIntent.OnNameChange("Entrenar"))
        viewModel.onIntent(AddHabitIntent.OnNextStep)
        viewModel.onIntent(AddHabitIntent.OnTypeChange(HabitType.TIME))
        viewModel.onIntent(AddHabitIntent.OnGoalValueChange("30"))

        viewModel.onIntent(AddHabitIntent.OnNextStep)

        assertEquals(3, viewModel.state.value.step)
        assertNull(viewModel.state.value.goalError)
    }

    @Test
    fun `OnNextStep from step 2 stays and sets error when goal is invalid`() {
        viewModel.onIntent(AddHabitIntent.OnNameChange("Entrenar"))
        viewModel.onIntent(AddHabitIntent.OnNextStep)
        viewModel.onIntent(AddHabitIntent.OnTypeChange(HabitType.TIME))
        viewModel.onIntent(AddHabitIntent.OnGoalValueChange("0"))

        viewModel.onIntent(AddHabitIntent.OnNextStep)

        assertEquals(2, viewModel.state.value.step)
        assertEquals("El objetivo debe ser mayor a 0", viewModel.state.value.goalError)
    }

    @Test
    fun `OnNextStep from step 2 stays when metric unit is missing`() {
        viewModel.onIntent(AddHabitIntent.OnNameChange("Hidratacion"))
        viewModel.onIntent(AddHabitIntent.OnNextStep)
        viewModel.onIntent(AddHabitIntent.OnTypeChange(HabitType.METRIC))
        viewModel.onIntent(AddHabitIntent.OnGoalValueChange("8"))
        viewModel.onIntent(AddHabitIntent.OnUnitChange(""))

        viewModel.onIntent(AddHabitIntent.OnNextStep)

        assertEquals(2, viewModel.state.value.step)
        assertEquals("Ingresa la unidad de medida", viewModel.state.value.unitError)
        assertNull(viewModel.state.value.goalError)
    }

    @Test
    fun `OnPreviousStep never goes below step 1`() {
        viewModel.onIntent(AddHabitIntent.OnPreviousStep)

        assertEquals(1, viewModel.state.value.step)
    }

    @Test
    fun `OnDayToggle removes and adds day`() {
        viewModel.onIntent(AddHabitIntent.OnDayToggle(DayOfWeek.MONDAY))
        assertEquals(false, viewModel.state.value.scheduledDays.contains(DayOfWeek.MONDAY))

        viewModel.onIntent(AddHabitIntent.OnDayToggle(DayOfWeek.MONDAY))
        assertEquals(true, viewModel.state.value.scheduledDays.contains(DayOfWeek.MONDAY))
    }

    @Test
    fun `form field intents update state values`() {
        viewModel.onIntent(AddHabitIntent.OnIconChange("Run"))
        viewModel.onIntent(AddHabitIntent.OnCategoryChange("Fitness"))
        viewModel.onIntent(AddHabitIntent.OnUnitChange("min"))

        val state = viewModel.state.value
        assertEquals("Run", state.icon)
        assertEquals("Fitness", state.category)
        assertEquals("min", state.unit)
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
    fun `OnSaveClick keeps user on screen when no days are selected`() = runTest {
        viewModel.onIntent(AddHabitIntent.OnNameChange("Leer"))
        DayOfWeek.entries.forEach { day ->
            viewModel.onIntent(AddHabitIntent.OnDayToggle(day))
        }

        viewModel.effect.test {
            viewModel.onIntent(AddHabitIntent.OnSaveClick)
            advanceUntilIdle()

            expectNoEvents()
        }

        coVerify(exactly = 0) { createHabitUseCase.invoke(any()) }
        assertEquals(
            "Selecciona al menos un día para este hábito",
            viewModel.state.value.scheduledDaysError,
        )
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

    @Test
    fun `OnSaveClick in edit mode calls update use case`() = runTest {
        val existingHabit = Habit(
            id = "habit-1",
            name = "Leer",
            icon = "FitnessCenter",
            color = 1,
            category = "Mente",
            type = HabitType.TIME,
            goalValue = 30f,
            unit = "min",
            scheduledDays = DayOfWeek.entries.toSet(),
        )
        coEvery { getHabitByIdUseCase.invoke("habit-1") } returns existingHabit
        coEvery { updateHabitUseCase.invoke(any()) } returns Unit

        viewModel.onIntent(AddHabitIntent.LoadHabitForEdit("habit-1"))
        advanceUntilIdle()
        viewModel.onIntent(AddHabitIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateHabitUseCase.invoke(any()) }
        coVerify(exactly = 0) { createHabitUseCase.invoke(any()) }
    }

    @Test
    fun `OnReminderEnabledToggle sets reminderEnabled and preserves time`() {
        viewModel.onIntent(AddHabitIntent.OnTimePickerConfirm(8, 30))
        viewModel.onIntent(AddHabitIntent.OnReminderEnabledToggle(false))

        val state = viewModel.state.value
        assertFalse(state.reminderEnabled)
        assertEquals(8, state.reminderHour)
        assertEquals(30, state.reminderMinute)
    }

    @Test
    fun `OnTimePickerConfirm updates hour and minute and closes picker`() {
        viewModel.onIntent(AddHabitIntent.OnTimePickerOpen)
        viewModel.onIntent(AddHabitIntent.OnTimePickerConfirm(7, 45))

        val state = viewModel.state.value
        assertEquals(7, state.reminderHour)
        assertEquals(45, state.reminderMinute)
        assertFalse(state.showTimePicker)
    }

    @Test
    fun `OnTimePickerOpen opens picker`() {
        viewModel.onIntent(AddHabitIntent.OnTimePickerOpen)

        assertTrue(viewModel.state.value.showTimePicker)
    }

    @Test
    fun `OnTimePickerDismiss closes picker without changing time`() {
        viewModel.onIntent(AddHabitIntent.OnTimePickerOpen)
        viewModel.onIntent(AddHabitIntent.OnTimePickerConfirm(8, 0))
        viewModel.onIntent(AddHabitIntent.OnTimePickerOpen)
        viewModel.onIntent(AddHabitIntent.OnTimePickerDismiss)

        val state = viewModel.state.value
        assertFalse(state.showTimePicker)
        assertEquals(8, state.reminderHour)
    }

    @Test
    fun `OnSaveClick passes reminder fields to use case`() = runTest {
        val capturedHabit = slot<Habit>()
        coEvery { createHabitUseCase.invoke(capture(capturedHabit)) } returns Unit

        viewModel.onIntent(AddHabitIntent.OnNameChange("Cardio"))
        viewModel.onIntent(AddHabitIntent.OnTimePickerConfirm(8, 30))
        viewModel.onIntent(AddHabitIntent.OnReminderEnabledToggle(true))

        viewModel.effect.test {
            viewModel.onIntent(AddHabitIntent.OnSaveClick)
            advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(true, capturedHabit.captured.reminderEnabled)
        assertEquals(8, capturedHabit.captured.reminderHour)
        assertEquals(30, capturedHabit.captured.reminderMinute)
    }

    @Test
    fun `OnSaveClick with reminderEnabled=false passes false but retains time`() = runTest {
        val capturedHabit = slot<Habit>()
        coEvery { createHabitUseCase.invoke(capture(capturedHabit)) } returns Unit

        viewModel.onIntent(AddHabitIntent.OnNameChange("Yoga"))
        viewModel.onIntent(AddHabitIntent.OnTimePickerConfirm(8, 30))
        viewModel.onIntent(AddHabitIntent.OnReminderEnabledToggle(false))

        viewModel.effect.test {
            viewModel.onIntent(AddHabitIntent.OnSaveClick)
            advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertFalse(capturedHabit.captured.reminderEnabled)
        assertEquals(8, capturedHabit.captured.reminderHour)
        assertEquals(30, capturedHabit.captured.reminderMinute)
    }

    @Test
    fun `OnSaveClick without custom reminder time persists null and uses global default`() = runTest {
        val capturedHabit = slot<Habit>()
        coEvery { createHabitUseCase.invoke(capture(capturedHabit)) } returns Unit

        viewModel.onIntent(AddHabitIntent.OnNameChange("Caminar"))

        viewModel.effect.test {
            viewModel.onIntent(AddHabitIntent.OnSaveClick)
            advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(true, capturedHabit.captured.reminderEnabled)
        assertNull(capturedHabit.captured.reminderHour)
        assertNull(capturedHabit.captured.reminderMinute)
    }
}

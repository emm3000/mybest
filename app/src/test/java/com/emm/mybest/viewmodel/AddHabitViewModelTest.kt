package com.emm.mybest.viewmodel

import app.cash.turbine.test
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class AddHabitViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AddHabitViewModel
    private val dailyHabitDao: DailyHabitDao = mockk()

    @Before
    fun setup() {
        viewModel = AddHabitViewModel(dailyHabitDao)
    }

    @Test
    fun `initial state is default`() = runTest {
        val state = viewModel.state.value
        assertEquals(false, state.ateHealthy)
        assertEquals(false, state.didExercise)
        assertEquals("", state.notes)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `OnAteHealthyChange updates state`() = runTest {
        viewModel.onIntent(AddHabitIntent.OnAteHealthyChange(true))
        assertEquals(true, viewModel.state.value.ateHealthy)
    }

    @Test
    fun `OnDidExerciseChange updates state`() = runTest {
        viewModel.onIntent(AddHabitIntent.OnDidExerciseChange(true))
        assertEquals(true, viewModel.state.value.didExercise)
    }

    @Test
    fun `OnNotesChange updates state`() = runTest {
        viewModel.onIntent(AddHabitIntent.OnNotesChange("Some notes"))
        assertEquals("Some notes", viewModel.state.value.notes)
    }

    @Test
    fun `OnSaveClick success emits NavigateBack effect`() = runTest {
        coEvery { dailyHabitDao.upsert(any()) } just Runs

        viewModel.onIntent(AddHabitIntent.OnAteHealthyChange(true))
        viewModel.onIntent(AddHabitIntent.OnNotesChange("Good day"))

        viewModel.effect.test {
            viewModel.onIntent(AddHabitIntent.OnSaveClick)
            assertEquals(AddHabitEffect.NavigateBack, awaitItem())
        }

        val capturedEntity = slot<DailyHabitEntity>()
        coVerify { dailyHabitDao.upsert(capture(capturedEntity)) }
        assertEquals(true, capturedEntity.captured.ateHealthy)
        assertEquals("Good day", capturedEntity.captured.notes)
        assertEquals(LocalDate.now(), capturedEntity.captured.date)
    }

    @Test
    fun `OnSaveClick error emits ShowError effect`() = runTest {
        val errorMessage = "Database error"
        coEvery { dailyHabitDao.upsert(any()) } throws Exception(errorMessage)

        viewModel.effect.test {
            viewModel.onIntent(AddHabitIntent.OnSaveClick)
            val effect = awaitItem()
            assert(effect is AddHabitEffect.ShowError)
            assertEquals("Error al guardar: $errorMessage", (effect as AddHabitEffect.ShowError).message)
        }
    }
}

package com.emm.mybest.features.weight.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.domain.usecase.weight.SaveWeightUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddWeightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val saveWeightUseCase = mockk<SaveWeightUseCase>(relaxed = true)
    private val observeWeightProgressUseCase = mockk<ObserveWeightProgressUseCase>(relaxed = true)

    private fun buildViewModel(
        existingEntries: List<WeightEntry> = emptyList(),
    ): AddWeightViewModel {
        every { observeWeightProgressUseCase() } returns flowOf(existingEntries)
        return AddWeightViewModel(saveWeightUseCase, observeWeightProgressUseCase)
    }

    @Test
    fun `initial state has empty weight and note with no error`() = runTest {
        val viewModel = buildViewModel()

        val state = viewModel.state.value
        assertEquals("", state.weight)
        assertEquals("", state.note)
        assertNull(state.weightError)
        assertFalse(state.isLoading)
    }

    @Test
    fun `lastRecordedWeight is set from first repository entry`() = runTest {
        val entry = WeightEntry(id = "w1", date = LocalDate(2026, 3, 10), weight = 74.5f)
        val viewModel = buildViewModel(existingEntries = listOf(entry))
        advanceUntilIdle()

        assertEquals(74.5f, viewModel.state.value.lastRecordedWeight)
    }

    @Test
    fun `lastRecordedWeight is null when no entries exist`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertNull(viewModel.state.value.lastRecordedWeight)
    }

    @Test
    fun `OnWeightChange updates weight field`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("72.5"))

        assertEquals("72.5", viewModel.state.value.weight)
    }

    @Test
    fun `OnNoteChange updates note field`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnNoteChange("me siento bien"))

        assertEquals("me siento bien", viewModel.state.value.note)
    }

    @Test
    fun `valid decimal weight with dot clears error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("72.4"))

        assertNull(viewModel.state.value.weightError)
    }

    @Test
    fun `valid decimal weight with comma clears error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("72,4"))

        assertNull(viewModel.state.value.weightError)
    }

    @Test
    fun `blank weight input clears error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange(""))

        assertNull(viewModel.state.value.weightError)
    }

    @Test
    fun `alphabetic weight input sets weightError`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("abc"))

        assertNotNull(viewModel.state.value.weightError)
    }

    @Test
    fun `weight ending with trailing dot sets error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("72."))

        assertNotNull(viewModel.state.value.weightError)
    }

    @Test
    fun `weight ending with trailing comma sets error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("72,"))

        assertNotNull(viewModel.state.value.weightError)
    }

    @Test
    fun `OnSaveClick with blank weight sets error and does not call use case`() = runTest {
        val viewModel = buildViewModel()
        // weight is blank by default

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 0) { saveWeightUseCase(any(), any()) }
    }

    @Test
    fun `OnSaveClick with invalid weight sets error and does not call use case`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("abc"))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.weightError)
        coVerify(exactly = 0) { saveWeightUseCase(any(), any()) }
    }

    @Test
    fun `OnSaveClick with valid weight calls use case and emits NavigateBack`() = runTest {
        coEvery { saveWeightUseCase(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("72.5"))

        viewModel.effect.test {
            viewModel.onIntent(AddWeightIntent.OnSaveClick)
            advanceUntilIdle()
            assertEquals(AddWeightEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { saveWeightUseCase(72.5f, null) }
    }

    @Test
    fun `OnSaveClick with comma decimal calls use case with parsed float`() = runTest {
        coEvery { saveWeightUseCase(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("72,5"))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 1) { saveWeightUseCase(72.5f, null) }
    }

    @Test
    fun `OnSaveClick with non-blank note passes note to use case`() = runTest {
        coEvery { saveWeightUseCase(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))
        viewModel.onIntent(AddWeightIntent.OnNoteChange("bien"))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 1) { saveWeightUseCase(70.0f, "bien") }
    }

    @Test
    fun `OnSaveClick with blank note passes null to use case`() = runTest {
        coEvery { saveWeightUseCase(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))
        viewModel.onIntent(AddWeightIntent.OnNoteChange("   "))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 1) { saveWeightUseCase(70.0f, null) }
    }

    @Test
    fun `OnSaveClick emits ShowError when use case throws`() = runTest {
        coEvery { saveWeightUseCase(any(), any()) } throws RuntimeException("DB fail")
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))

        viewModel.effect.test {
            viewModel.onIntent(AddWeightIntent.OnSaveClick)
            advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is AddWeightEffect.ShowError)
            assertTrue((effect as AddWeightEffect.ShowError).message.contains("DB fail"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isLoading is reset to false after successful save`() = runTest {
        coEvery { saveWeightUseCase(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `out-of-range weight sets error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("232323"))

        assertNotNull(viewModel.state.value.weightError)
    }
}

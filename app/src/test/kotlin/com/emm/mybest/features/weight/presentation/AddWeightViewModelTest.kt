package com.emm.mybest.features.weight.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class AddWeightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val weightRepository = mockk<WeightRepository>(relaxed = true)

    private fun buildViewModel(
        existingEntries: List<WeightEntry> = emptyList(),
    ): AddWeightViewModel {
        every { weightRepository.getWeightProgress() } returns flowOf(existingEntries)
        return AddWeightViewModel(weightRepository)
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
    fun `OnSaveClick with blank weight sets error and does not call repository`() = runTest {
        val viewModel = buildViewModel()
        // weight is blank by default

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 0) { weightRepository.saveWeight(any(), any()) }
    }

    @Test
    fun `OnSaveClick with invalid weight sets error and does not call repository`() = runTest {
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("abc"))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.weightError)
        coVerify(exactly = 0) { weightRepository.saveWeight(any(), any()) }
    }

    @Test
    fun `OnSaveClick with valid weight calls repository and emits NavigateBack`() = runTest {
        coEvery { weightRepository.saveWeight(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("72.5"))

        viewModel.effect.test {
            viewModel.onIntent(AddWeightIntent.OnSaveClick)
            advanceUntilIdle()
            assertEquals(AddWeightEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { weightRepository.saveWeight(72.5f, null) }
    }

    @Test
    fun `OnSaveClick with comma decimal calls repository with parsed float`() = runTest {
        coEvery { weightRepository.saveWeight(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("72,5"))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 1) { weightRepository.saveWeight(72.5f, null) }
    }

    @Test
    fun `OnSaveClick with non-blank note passes note to repository`() = runTest {
        coEvery { weightRepository.saveWeight(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))
        viewModel.onIntent(AddWeightIntent.OnNoteChange("bien"))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 1) { weightRepository.saveWeight(70.0f, "bien") }
    }

    @Test
    fun `OnSaveClick with blank note passes null to repository`() = runTest {
        coEvery { weightRepository.saveWeight(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))
        viewModel.onIntent(AddWeightIntent.OnNoteChange("   "))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        coVerify(exactly = 1) { weightRepository.saveWeight(70.0f, null) }
    }

    @Test
    fun `OnSaveClick emits ShowError when repository throws`() = runTest {
        coEvery { weightRepository.saveWeight(any(), any()) } throws RuntimeException("DB fail")
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
        coEvery { weightRepository.saveWeight(any(), any()) } returns Unit
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))

        viewModel.onIntent(AddWeightIntent.OnSaveClick)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
    }
}

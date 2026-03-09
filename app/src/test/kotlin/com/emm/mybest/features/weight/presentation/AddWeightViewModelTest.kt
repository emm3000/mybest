package com.emm.mybest.features.weight.presentation

import app.cash.turbine.test
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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddWeightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<WeightRepository>()

    private fun buildViewModel(): AddWeightViewModel {
        every { repository.getWeightProgress() } returns flowOf(emptyList())
        return AddWeightViewModel(repository)
    }

    @Test
    fun `OnWeightChange and OnNoteChange update state`() {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("77.5"))
        viewModel.onIntent(AddWeightIntent.OnNoteChange("post gym"))

        assertEquals("77.5", viewModel.state.value.weight)
        assertEquals("post gym", viewModel.state.value.note)
    }

    @Test
    fun `OnSaveClick updates inline error when weight is invalid`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnSaveClick)

        assertEquals(
            "Ingresa un peso valido. Ejemplo: 72.4 o 72,4",
            viewModel.state.value.weightError,
        )
    }

    @Test
    fun `OnSaveClick success calls repository and emits NavigateBack`() = runTest {
        every { repository.getWeightProgress() } returns flowOf(emptyList())
        coEvery { repository.saveWeight(any(), any()) } returns Unit
        val viewModel = AddWeightViewModel(repository)
        viewModel.onIntent(AddWeightIntent.OnWeightChange("76.8"))
        viewModel.onIntent(AddWeightIntent.OnNoteChange("ok"))

        viewModel.effect.test {
            viewModel.onIntent(AddWeightIntent.OnSaveClick)
            assertEquals(AddWeightEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.saveWeight(76.8f, "ok") }
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `OnSaveClick sends null note when blank and handles repository error`() = runTest {
        every { repository.getWeightProgress() } returns flowOf(emptyList())
        coEvery { repository.saveWeight(any(), any()) } throws IllegalStateException("write fail")
        val viewModel = AddWeightViewModel(repository)
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))
        viewModel.onIntent(AddWeightIntent.OnNoteChange("   "))

        viewModel.effect.test {
            viewModel.onIntent(AddWeightIntent.OnSaveClick)
            advanceUntilIdle()

            assertEquals(AddWeightEffect.ShowError("Error al guardar: write fail"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.saveWeight(70.0f, null) }
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `OnWeightChange accepts comma decimal and clears error when valid`() {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("72,4"))

        assertEquals("72,4", viewModel.state.value.weight)
        assertEquals(null, viewModel.state.value.weightError)
    }
}

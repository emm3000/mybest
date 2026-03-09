package com.emm.mybest.features.weight.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun `OnWeightChange and OnNoteChange update state`() {
        val viewModel = AddWeightViewModel(repository)

        viewModel.onIntent(AddWeightIntent.OnWeightChange("77.5"))
        viewModel.onIntent(AddWeightIntent.OnNoteChange("post gym"))

        assertEquals("77.5", viewModel.state.value.weight)
        assertEquals("post gym", viewModel.state.value.note)
    }

    @Test
    fun `OnSaveClick emits error when weight is invalid`() = runTest {
        val viewModel = AddWeightViewModel(repository)

        viewModel.effect.test {
            viewModel.onIntent(AddWeightIntent.OnSaveClick)
            assertEquals(AddWeightEffect.ShowError("Peso inválido"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSaveClick success calls repository and emits NavigateBack`() = runTest {
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
}

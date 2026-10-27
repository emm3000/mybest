package com.emm.mybest.features.weight.presentation

import app.cash.turbine.test
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddWeightViewModelSaveTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixture = newAddWeightFixture()
    private val saveWeightUseCase = fixture.saveWeightUseCase

    private fun buildViewModel(): AddWeightViewModel = fixture.buildViewModel()

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

        assertEquals(AddWeightFieldError.INVALID_FORMAT, viewModel.state.value.weightError)
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
    fun `OnSaveClick emits neither ShowError nor NavigateBack when the save is cancelled`() = runTest {
        coEvery { saveWeightUseCase(any(), any()) } throws CancellationException("cancelled")
        val viewModel = buildViewModel()
        viewModel.onIntent(AddWeightIntent.OnWeightChange("70.0"))

        viewModel.effect.test {
            viewModel.onIntent(AddWeightIntent.OnSaveClick)
            advanceUntilIdle()
            expectNoEvents()
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
}

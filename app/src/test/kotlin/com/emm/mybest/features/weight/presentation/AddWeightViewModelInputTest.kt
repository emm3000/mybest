package com.emm.mybest.features.weight.presentation

import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class AddWeightViewModelInputTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixture = newAddWeightFixture()

    private fun buildViewModel(): AddWeightViewModel = fixture.buildViewModel()

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

        assertEquals(AddWeightFieldError.INVALID_FORMAT, viewModel.state.value.weightError)
    }

    @Test
    fun `weight ending with trailing dot sets error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("72."))

        assertEquals(AddWeightFieldError.INVALID_FORMAT, viewModel.state.value.weightError)
    }

    @Test
    fun `weight ending with trailing comma sets error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("72,"))

        assertEquals(AddWeightFieldError.INVALID_FORMAT, viewModel.state.value.weightError)
    }

    @Test
    fun `out-of-range weight sets the out-of-range error`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onIntent(AddWeightIntent.OnWeightChange("600"))

        assertEquals(AddWeightFieldError.OUT_OF_RANGE, viewModel.state.value.weightError)
    }
}

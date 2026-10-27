package com.emm.mybest.features.weight.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class AddWeightViewModelStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fixture = newAddWeightFixture()

    private fun buildViewModel(
        trend: WeighInTrend = WeighInTrend(),
        setup: Setup = Setup(),
    ): AddWeightViewModel = fixture.buildViewModel(trend, setup)

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
    fun `state exposes the weigh-in trend`() = runTest {
        val trend = WeighInTrend(latestKg = 74.5f, changeSincePreviousKg = -0.6f, remainingToTargetKg = 4.2f)
        val viewModel = buildViewModel(trend = trend)
        advanceUntilIdle()

        assertEquals(trend, viewModel.state.value.trend)
    }

    @Test
    fun `state exposes target weight and weigh-in day from setup`() = runTest {
        val setup = Setup(targetWeightKg = 78f, weighInDay = DayOfWeek.MONDAY)
        val viewModel = buildViewModel(setup = setup)
        advanceUntilIdle()

        assertEquals(78f, viewModel.state.value.targetWeightKg)
        assertEquals(DayOfWeek.MONDAY, viewModel.state.value.weighInDay)
    }

    @Test
    fun `state exposes the weigh-in day as a lowercase label`() = runTest {
        val viewModel = buildViewModel(setup = Setup(weighInDay = DayOfWeek.MONDAY))
        advanceUntilIdle()

        assertEquals("lunes", viewModel.state.value.weighInDayLabel)
    }

    @Test
    fun `unset setup leaves target and weigh-in day null`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertNull(viewModel.state.value.targetWeightKg)
        assertNull(viewModel.state.value.weighInDay)
    }

    @Test
    fun `OnOpenSettingsClick emits OpenSettings`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(AddWeightIntent.OnOpenSettingsClick)
            assertEquals(AddWeightEffect.OpenSettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

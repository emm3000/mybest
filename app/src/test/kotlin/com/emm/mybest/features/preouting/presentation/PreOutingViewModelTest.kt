package com.emm.mybest.features.preouting.presentation

import app.cash.turbine.test
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.usecase.preouting.ObservePreOutingSnapshotUseCase
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private val EMPTY_SNAPSHOT = PreOutingSnapshot(
    spentOnOutingsThisMonth = Money.ZERO,
    outingsThisMonth = 0,
    monthlyOutingLimit = null,
    isNextOutingOverLimit = false,
    estimatedOutingCost = null,
    isEstimateAveraged = false,
    nearestInstallment = null,
    purchaseGoalLeft = null,
    notSpentThisMonth = Money.ZERO,
)

private val FULL_SNAPSHOT = PreOutingSnapshot(
    spentOnOutingsThisMonth = Money(760_00),
    outingsThisMonth = 2,
    monthlyOutingLimit = 2,
    isNextOutingOverLimit = true,
    estimatedOutingCost = Money(380_00),
    isEstimateAveraged = true,
    nearestInstallment = InstallmentShare("Banco", LocalDate(2026, 11, 15), Money(420_00), nightPercent = 90),
    purchaseGoalLeft = Money(270_00),
    notSpentThisMonth = Money(380_00),
)

class PreOutingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        snapshot: PreOutingSnapshot,
        goal: PurchaseGoal? = null,
    ): PreOutingViewModel {
        val observeSnapshot = mockk<ObservePreOutingSnapshotUseCase>()
        every { observeSnapshot() } returns flowOf(snapshot)
        return PreOutingViewModel(observeSnapshot, FakePurchaseGoalRepository(goal))
    }

    private suspend fun kotlinx.coroutines.test.TestScope.stateOf(viewModel: PreOutingViewModel): PreOutingState {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        return viewModel.state.value
    }

    @Test
    fun `an empty setup asks for every missing input`() = runTest {
        val state = stateOf(viewModel(EMPTY_SNAPSHOT))

        assertEquals(PreOutingPrompt.entries.toSet(), state.prompts)
        assertTrue(state.isLoaded)
        assertNull(state.nightPercent)
        assertNull(state.goalLeftLabel)
    }

    @Test
    fun `a full setup asks for nothing and formats the money`() = runTest {
        val state = stateOf(viewModel(FULL_SNAPSHOT, PurchaseGoal("zapatillas", Money(370_00))))

        assertTrue(state.prompts.isEmpty())
        assertEquals("S/ 760.00", state.spentOnOutingsLabel)
        assertEquals("S/ 380.00", state.estimatedOutingCostLabel)
        assertTrue(state.isEstimateAveraged)
        assertEquals("S/ 270.00", state.goalLeftLabel)
        assertEquals("zapatillas", state.goalName)
        assertEquals("S/ 380.00", state.notSpentLabel)
        assertEquals(90, state.nightPercent)
        assertEquals("Banco", state.debtName)
        assertEquals(15, state.paymentDay)
        assertTrue(state.isNextOutingOverLimit)
        assertEquals(2, state.outingsThisMonth)
        assertEquals(2, state.monthlyOutingLimit)
    }

    @Test
    fun `a declared typical cost is not labeled as an average`() = runTest {
        val declared = FULL_SNAPSHOT.copy(isEstimateAveraged = false)

        val state = stateOf(viewModel(declared, PurchaseGoal("zapatillas", Money(370_00))))

        assertEquals("S/ 380.00", state.estimatedOutingCostLabel)
        assertFalse(state.isEstimateAveraged)
    }

    @Test
    fun `debts without an estimate ask only for the typical cost`() = runTest {
        val noEstimate = FULL_SNAPSHOT.copy(
            estimatedOutingCost = null,
            nearestInstallment = FULL_SNAPSHOT.nearestInstallment?.copy(nightPercent = null),
        )

        val state = stateOf(viewModel(noEstimate, PurchaseGoal("zapatillas", Money(370_00))))

        assertEquals(setOf(PreOutingPrompt.TYPICAL_COST), state.prompts)
        assertNull(state.nightPercent)
    }

    @Test
    fun `tapping a prompt opens settings`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)

        viewModel.effect.test {
            viewModel.onIntent(PreOutingIntent.OnPromptClick(PreOutingPrompt.DEBTS))
            assertEquals(PreOutingEffect.OpenSettings, awaitItem())
        }
        assertFalse(viewModel.state.value.isLoaded)
    }
}

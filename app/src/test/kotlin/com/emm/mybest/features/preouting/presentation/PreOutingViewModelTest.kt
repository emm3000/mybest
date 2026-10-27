package com.emm.mybest.features.preouting.presentation

import app.cash.turbine.test
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PreOutingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val outingRepository = FakeOutingRepository()
    private val gatedOutingRepository = GatedOutingRepository(outingRepository)

    private fun viewModel(
        snapshot: PreOutingSnapshot,
        goal: PurchaseGoal? = null,
        scenario: Scenario = Scenario(),
    ): PreOutingViewModel =
        preOutingViewModel(snapshot, goal, decisionUseCases(scenario, outingRepository, gatedOutingRepository))

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
    fun `no debts at all asks for debts`() = runTest {
        val noDebts = FULL_SNAPSHOT.copy(hasDebts = false, nearestInstallment = null)

        val state = stateOf(viewModel(noDebts, PurchaseGoal("zapatillas", Money(370_00))))

        assertTrue(PreOutingPrompt.DEBTS in state.prompts)
    }

    @Test
    fun `only paid-off debts ask for nothing and show no night percent`() = runTest {
        val paidOff = FULL_SNAPSHOT.copy(hasDebts = true, nearestInstallment = null)

        val state = stateOf(viewModel(paidOff, PurchaseGoal("zapatillas", Money(370_00))))

        assertFalse(PreOutingPrompt.DEBTS in state.prompts)
        assertNull(state.nightPercent)
    }

    @Test
    fun `typical cost and outing limit prompts open settings`() = runTest {
        assertPromptOpens(PreOutingPrompt.TYPICAL_COST, PreOutingEffect.OpenSettings)
        assertPromptOpens(PreOutingPrompt.OUTING_LIMIT, PreOutingEffect.OpenSettings)
    }

    @Test
    fun `the debts prompt opens debts`() = runTest {
        assertPromptOpens(PreOutingPrompt.DEBTS, PreOutingEffect.OpenDebts)
    }

    @Test
    fun `the purchase goal prompt opens the purchase goal`() = runTest {
        assertPromptOpens(PreOutingPrompt.PURCHASE_GOAL, PreOutingEffect.OpenPurchaseGoal)
    }

    private suspend fun TestScope.assertPromptOpens(prompt: PreOutingPrompt, expected: PreOutingEffect) {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        viewModel.effect.test {
            viewModel.onIntent(PreOutingIntent.OnPromptClick(prompt))
            assertEquals(expected, awaitItem())
        }
        assertFalse(stateNow(viewModel).isLoaded)
    }
}

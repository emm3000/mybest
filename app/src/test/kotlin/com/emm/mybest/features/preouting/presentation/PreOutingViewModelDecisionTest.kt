package com.emm.mybest.features.preouting.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PreOutingViewModelDecisionTest {

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
    fun `starts undecided with both actions on a Friday`() = runTest {
        val state = stateOf(viewModel(EMPTY_SNAPSHOT))

        assertEquals(PreOutingDecisionState.Undecided, state.decision)
        assertTrue(state.canStayIn)
        assertFalse(state.form.isOpen)
        assertEquals("VIE · 23 OCT", state.tonightLabel)
    }

    @Test
    fun `staying in is not offered outside Friday and Saturday`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT, scenario = Scenario(now = LocalDateTime(THURSDAY, LocalTime(21, 0))))

        val state = stateOf(viewModel)
        viewModel.onIntent(PreOutingIntent.OnStayInClick)
        advanceUntilIdle()

        assertFalse(state.canStayIn)
        assertEquals(PreOutingDecisionState.Undecided, stateNow(viewModel).decision)
    }

    @Test
    fun `staying in outside Friday and Saturday warns and leaves the night undecided`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT, scenario = Scenario(now = LocalDateTime(THURSDAY, LocalTime(21, 0))))
        stateOf(viewModel)

        viewModel.effect.test {
            viewModel.onIntent(PreOutingIntent.OnStayInClick)
            assertEquals(PreOutingEffect.NotWonNightDay, awaitItem())
        }
        assertEquals(PreOutingDecisionState.Undecided, stateNow(viewModel).decision)
    }

    @Test
    fun `confirming records the planned outing and shows it`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)

        viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
        advanceUntilIdle()

        val decision = stateNow(viewModel).decision
        assertEquals(PreOutingDecisionState.WentOut("S/ 150.00", "4", "01:30"), decision)
        assertFalse(stateNow(viewModel).form.isOpen)
    }

    @Test
    fun `staying in records the won night with the estimate`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)

        viewModel.onIntent(PreOutingIntent.OnStayInClick)
        advanceUntilIdle()

        assertEquals(PreOutingDecisionState.StayedIn("S/ 380.00"), stateNow(viewModel).decision)
    }

    @Test
    fun `staying in without an estimate shows no amount`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT, scenario = Scenario(setup = Setup()))
        stateOf(viewModel)

        viewModel.onIntent(PreOutingIntent.OnStayInClick)
        advanceUntilIdle()

        assertEquals(PreOutingDecisionState.StayedIn(estimateLabel = null), stateNow(viewModel).decision)
    }

    @Test
    fun `confirming on a day that already has a decision keeps the form and warns`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)
        outingRepository.recordUnplanned(FRIDAY)

        viewModel.effect.test {
            viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
            assertEquals(PreOutingEffect.DayAlreadyDecided, awaitItem())
        }
        assertTrue(stateNow(viewModel).form.isOpen)
    }

    @Test
    fun `staying in on a day that already has a decision warns`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        outingRepository.recordUnplanned(FRIDAY)

        viewModel.effect.test {
            viewModel.onIntent(PreOutingIntent.OnStayInClick)
            assertEquals(PreOutingEffect.DayAlreadyDecided, awaitItem())
        }
    }
}

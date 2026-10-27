package com.emm.mybest.features.preouting.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class PreOutingViewModelSaveTest {

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
    fun `confirming three times while the outing is being saved records one outing`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)
        val gate = CompletableDeferred<Unit>()
        gatedOutingRepository.gate = gate

        viewModel.effect.test {
            repeat(3) { viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick) }
            runCurrent()
            gate.complete(Unit)
            advanceUntilIdle()
            expectNoEvents()
        }

        assertEquals(1, gatedOutingRepository.recordPlannedCalls)
        assertEquals(1, outingRepository.observeAll().first().size)
        assertFalse(stateNow(viewModel).form.isOpen)
    }

    @Test
    fun `confirming again after an interrupted save attempts the save again`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)
        gatedOutingRepository.failure = CancellationException("save interrupted")

        viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
        advanceUntilIdle()
        gatedOutingRepository.failure = null
        viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
        advanceUntilIdle()

        assertEquals(2, gatedOutingRepository.recordPlannedCalls)
        assertEquals(1, outingRepository.observeAll().first().size)
    }

    @Test
    fun `a failed save reports the failure and keeps the form as filled`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        openFilledForm(viewModel)
        gatedOutingRepository.failure = IllegalStateException("disk full")

        viewModel.effect.test {
            viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
            assertEquals(PreOutingEffect.ActionFailed, awaitItem())
            expectNoEvents()
        }

        assertFilledFormKept(stateNow(viewModel))
    }

    @Test
    fun `confirming again after a failed save saves the outing`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        openFilledForm(viewModel)
        gatedOutingRepository.failure = IllegalStateException("disk full")
        confirm(viewModel)
        gatedOutingRepository.failure = null

        viewModel.effect.test {
            confirm(viewModel)
            expectNoEvents()
        }

        assertEquals(2, gatedOutingRepository.recordPlannedCalls)
        assertEquals(1, outingRepository.observeAll().first().size)
        assertFalse(stateNow(viewModel).form.isOpen)
    }

    @Test
    fun `an interrupted save does not report a failure`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        openFilledForm(viewModel)
        gatedOutingRepository.failure = CancellationException("save interrupted")

        viewModel.effect.test {
            confirm(viewModel)
            expectNoEvents()
        }
    }
}

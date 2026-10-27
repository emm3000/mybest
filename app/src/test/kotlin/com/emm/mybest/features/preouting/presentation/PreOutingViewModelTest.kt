package com.emm.mybest.features.preouting.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.usecase.preouting.GetEstimatedOutingCostUseCase
import com.emm.mybest.domain.usecase.preouting.HasDecisionOnUseCase
import com.emm.mybest.domain.usecase.preouting.ObservePreOutingSnapshotUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveTonightUseCase
import com.emm.mybest.domain.usecase.preouting.PlanOutingUseCase
import com.emm.mybest.domain.usecase.preouting.PreOutingDecisionUseCases
import com.emm.mybest.domain.usecase.preouting.RecordWonNightUseCase
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val THURSDAY = LocalDate(2026, 10, 22)

private val EMPTY_SNAPSHOT = PreOutingSnapshot(
    spentOnOutingsThisMonth = Money.ZERO,
    outingsThisMonth = 0,
    monthlyOutingLimit = null,
    isNextOutingOverLimit = false,
    estimatedOutingCost = null,
    isEstimateAveraged = false,
    hasDebts = false,
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
    hasDebts = true,
    nearestInstallment = InstallmentShare("Banco", LocalDate(2026, 11, 15), Money(420_00), nightPercent = 90),
    purchaseGoalLeft = Money(270_00),
    notSpentThisMonth = Money(380_00),
)

private class GatedOutingRepository(
    private val delegate: OutingRepository,
) : OutingRepository by delegate {

    var gate: CompletableDeferred<Unit>? = null
    var failure: Throwable? = null
    var recordPlannedCalls = 0

    override suspend fun recordPlanned(appDay: LocalDate, limits: Limits): Long {
        recordPlannedCalls++
        failure?.let { throw it }
        gate?.await()
        return delegate.recordPlanned(appDay, limits)
    }
}

private fun TestScope.fillForm(viewModel: PreOutingViewModel) {
    viewModel.onIntent(PreOutingIntent.OnGoOutClick)
    viewModel.onIntent(PreOutingIntent.OnMoneyChange("150"))
    viewModel.onIntent(PreOutingIntent.OnDrinksChange("4"))
    viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(1, 30))
    viewModel.onIntent(PreOutingIntent.OnCardsOffToggle)
    viewModel.onIntent(PreOutingIntent.OnAccountLoadedToggle)
    advanceUntilIdle()
}

private fun TestScope.stateNow(viewModel: PreOutingViewModel): PreOutingState {
    advanceUntilIdle()
    return viewModel.state.value
}

private suspend fun TestScope.stateOf(viewModel: PreOutingViewModel): PreOutingState {
    backgroundScope.launch { viewModel.state.collect {} }
    advanceUntilIdle()
    return stateNow(viewModel)
}

private data class Scenario(
    val now: LocalDateTime = LocalDateTime(FRIDAY, LocalTime(21, 0)),
    val setup: Setup = Setup(typicalOutingCost = Money(380_00)),
)

private fun decisionUseCases(
    scenario: Scenario,
    outings: OutingRepository,
    gatedOutings: OutingRepository,
): PreOutingDecisionUseCases {
    val clock = AppDayClock(FixedClock(scenario.now.toInstant(LIMA)), LIMA)
    val estimate = GetEstimatedOutingCostUseCase(FakeSetupRepository(scenario.setup), outings)
    val hasDecisionOn = HasDecisionOnUseCase(outings)
    return PreOutingDecisionUseCases(
        observeTonight = ObserveTonightUseCase(outings, clock),
        planOuting = PlanOutingUseCase(gatedOutings, hasDecisionOn, clock),
        recordWonNight = RecordWonNightUseCase(outings, hasDecisionOn, estimate, clock),
    )
}

private fun preOutingViewModel(
    snapshot: PreOutingSnapshot,
    goal: PurchaseGoal?,
    decisions: PreOutingDecisionUseCases,
): PreOutingViewModel {
    val observeSnapshot = mockk<ObservePreOutingSnapshotUseCase>()
    every { observeSnapshot() } returns flowOf(snapshot)
    return PreOutingViewModel(observeSnapshot, FakePurchaseGoalRepository(goal), decisions)
}

private suspend fun TestScope.openFilledForm(viewModel: PreOutingViewModel) {
    stateOf(viewModel)
    fillForm(viewModel)
}

private fun TestScope.confirm(viewModel: PreOutingViewModel) {
    viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
    advanceUntilIdle()
}

private fun assertFilledFormKept(state: PreOutingState) {
    assertTrue(state.form.isOpen)
    assertEquals("150", state.form.moneyInput)
    assertEquals("4", state.form.drinksInput)
    assertEquals(LocalTime(1, 30), state.form.returnTime)
    assertTrue(state.form.areCardsOff)
    assertTrue(state.form.isAccountLoaded)
    assertEquals(PreOutingDecisionState.Undecided, state.decision)
}

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
    fun `going out opens the form and dismissing closes it`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)

        viewModel.onIntent(PreOutingIntent.OnGoOutClick)
        assertTrue(stateNow(viewModel).form.isOpen)
        viewModel.onIntent(PreOutingIntent.OnFormDismiss)

        assertFalse(stateNow(viewModel).form.isOpen)
    }

    @Test
    fun `confirming needs the three limits and both checklist items`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        viewModel.onIntent(PreOutingIntent.OnGoOutClick)
        viewModel.onIntent(PreOutingIntent.OnMoneyChange("150"))
        viewModel.onIntent(PreOutingIntent.OnDrinksChange("4"))
        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(1, 30))
        viewModel.onIntent(PreOutingIntent.OnCardsOffToggle)
        assertFalse(stateNow(viewModel).canConfirmOuting)

        viewModel.onIntent(PreOutingIntent.OnAccountLoadedToggle)

        assertTrue(stateNow(viewModel).canConfirmOuting)
        assertEquals("S/ 150.00", stateNow(viewModel).moneyLimitLabel)
        assertEquals("01:30", stateNow(viewModel).returnTimeLabel)
    }

    @Test
    fun `a money limit of zero or text cannot be confirmed`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        fillForm(viewModel)

        viewModel.onIntent(PreOutingIntent.OnMoneyChange("0"))
        assertFalse(stateNow(viewModel).canConfirmOuting)
        viewModel.onIntent(PreOutingIntent.OnMoneyChange("abc"))

        assertFalse(stateNow(viewModel).canConfirmOuting)
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
    fun `an incomplete form records nothing`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        viewModel.onIntent(PreOutingIntent.OnGoOutClick)

        viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
        advanceUntilIdle()

        assertEquals(PreOutingDecisionState.Undecided, stateNow(viewModel).decision)
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
    fun `the return time picker opens and closes`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        viewModel.onIntent(PreOutingIntent.OnGoOutClick)

        viewModel.onIntent(PreOutingIntent.OnReturnTimeClick)
        assertTrue(stateNow(viewModel).form.isTimePickerOpen)
        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(2, 0))

        assertFalse(stateNow(viewModel).form.isTimePickerOpen)
        assertEquals("02:00", stateNow(viewModel).returnTimeLabel)
    }

    @Test
    fun `the picker starts at 02-00 until a return time is chosen`() = runTest {
        val viewModel = viewModel(EMPTY_SNAPSHOT)
        stateOf(viewModel)
        assertEquals(LocalTime(2, 0), stateNow(viewModel).form.pickerTime)

        viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(1, 30))

        assertEquals(LocalTime(1, 30), stateNow(viewModel).form.pickerTime)
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

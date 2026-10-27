package com.emm.mybest.features.preouting.presentation

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
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

internal val LIMA = TimeZone.of("America/Lima")
internal val FRIDAY = LocalDate(2026, 10, 23)
internal val THURSDAY = LocalDate(2026, 10, 22)

internal val EMPTY_SNAPSHOT = PreOutingSnapshot(
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

internal val FULL_SNAPSHOT = PreOutingSnapshot(
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

internal class GatedOutingRepository(
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

internal fun TestScope.fillForm(viewModel: PreOutingViewModel) {
    viewModel.onIntent(PreOutingIntent.OnGoOutClick)
    viewModel.onIntent(PreOutingIntent.OnMoneyChange("150"))
    viewModel.onIntent(PreOutingIntent.OnDrinksChange("4"))
    viewModel.onIntent(PreOutingIntent.OnReturnTimeSelected(1, 30))
    viewModel.onIntent(PreOutingIntent.OnCardsOffToggle)
    viewModel.onIntent(PreOutingIntent.OnAccountLoadedToggle)
    advanceUntilIdle()
}

internal fun TestScope.stateNow(viewModel: PreOutingViewModel): PreOutingState {
    advanceUntilIdle()
    return viewModel.state.value
}

internal suspend fun TestScope.stateOf(viewModel: PreOutingViewModel): PreOutingState {
    backgroundScope.launch { viewModel.state.collect {} }
    advanceUntilIdle()
    return stateNow(viewModel)
}

internal data class Scenario(
    val now: LocalDateTime = LocalDateTime(FRIDAY, LocalTime(21, 0)),
    val setup: Setup = Setup(typicalOutingCost = Money(380_00)),
)

internal fun decisionUseCases(
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

internal fun preOutingViewModel(
    snapshot: PreOutingSnapshot,
    goal: PurchaseGoal?,
    decisions: PreOutingDecisionUseCases,
): PreOutingViewModel {
    val observeSnapshot = mockk<ObservePreOutingSnapshotUseCase>()
    every { observeSnapshot() } returns flowOf(snapshot)
    return PreOutingViewModel(observeSnapshot, FakePurchaseGoalRepository(goal), decisions)
}

internal suspend fun TestScope.openFilledForm(viewModel: PreOutingViewModel) {
    stateOf(viewModel)
    fillForm(viewModel)
}

internal fun TestScope.confirm(viewModel: PreOutingViewModel) {
    viewModel.onIntent(PreOutingIntent.OnConfirmOutingClick)
    advanceUntilIdle()
}

internal fun assertFilledFormKept(state: PreOutingState) {
    assertTrue(state.form.isOpen)
    assertEquals("150", state.form.moneyInput)
    assertEquals("4", state.form.drinksInput)
    assertEquals(LocalTime(1, 30), state.form.returnTime)
    assertTrue(state.form.areCardsOff)
    assertTrue(state.form.isAccountLoaded)
    assertEquals(PreOutingDecisionState.Undecided, state.decision)
}

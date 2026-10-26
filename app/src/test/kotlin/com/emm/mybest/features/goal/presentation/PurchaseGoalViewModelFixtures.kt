package com.emm.mybest.features.goal.presentation

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.usecase.goal.AddGoalContributionUseCase
import com.emm.mybest.domain.usecase.goal.ObserveGoalProgressUseCase
import com.emm.mybest.domain.usecase.goal.SavePurchaseGoalUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveNotSpentThisMonthUseCase
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private val LIMA = TimeZone.of("America/Lima")
private val CLOCK = AppDayClock(FixedClock(LocalDateTime(2026, 10, 23, 12, 0).toInstant(LIMA)), LIMA)

internal fun purchaseGoalViewModel(
    repository: PurchaseGoalRepository,
    wonNights: List<WonNight> = emptyList(),
) = PurchaseGoalViewModel(
    PurchaseGoalUseCases(
        observeGoalProgress = ObserveGoalProgressUseCase(repository),
        observeNotSpent = ObserveNotSpentThisMonthUseCase(FakeOutingRepository(initialWonNights = wonNights), CLOCK),
        saveGoal = SavePurchaseGoalUseCase(repository),
        addContribution = AddGoalContributionUseCase(repository, CLOCK),
        purchaseGoalRepository = repository,
    ),
)

internal fun TestScope.send(viewModel: PurchaseGoalViewModel, vararg intents: PurchaseGoalIntent): PurchaseGoalState {
    backgroundScope.launch { viewModel.state.collect {} }
    advanceUntilIdle()
    intents.forEach { viewModel.onIntent(it) }
    advanceUntilIdle()
    return viewModel.state.value
}

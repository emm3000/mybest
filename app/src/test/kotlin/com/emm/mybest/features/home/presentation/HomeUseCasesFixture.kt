package com.emm.mybest.features.home.presentation

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.checkin.ObserveCheckInTaskUseCase
import com.emm.mybest.domain.usecase.checkin.ObserveHasPendingCheckInUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveDailyChecksUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveWeekProgressUseCase
import com.emm.mybest.domain.usecase.dailycheck.ToggleDailyCheckUseCase
import com.emm.mybest.domain.usecase.home.ResolveHomeDestinationUseCase
import com.emm.mybest.domain.usecase.preouting.GetEstimatedOutingCostUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveNearestInstallmentUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveTonightUseCase
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeSetupRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

internal data class HomeUseCasesFixture(
    val weights: List<WeightEntry> = emptyList(),
    val photos: List<ProgressPhoto> = emptyList(),
    val dailyChecks: FakeDailyCheckRepository = FakeDailyCheckRepository(),
    val outings: FakeOutingRepository = FakeOutingRepository(),
    val debts: FakeDebtRepository = FakeDebtRepository(),
    val setup: Setup = Setup(),
) {
    private val setupRepository = FakeSetupRepository(setup)

    fun build(appDayClock: AppDayClock): HomeUseCases = HomeUseCases(
        observeWeightProgress = mockk { every { this@mockk() } returns flowOf(weights) },
        observePhotos = mockk { every { this@mockk() } returns flowOf(photos) },
        observeDailyChecks = ObserveDailyChecksUseCase(dailyChecks),
        observeWeekProgress = ObserveWeekProgressUseCase(dailyChecks, setupRepository),
        toggleDailyCheck = ToggleDailyCheckUseCase(dailyChecks),
        resolveDestination = ResolveHomeDestinationUseCase(
            ObserveCheckInTaskUseCase(outings, appDayClock),
            ObserveTonightUseCase(outings, appDayClock),
            appDayClock,
        ),
        observeHasPendingCheckIn = ObserveHasPendingCheckInUseCase(outings),
        observeNextInstallment = ObserveNearestInstallmentUseCase(
            debts,
            GetEstimatedOutingCostUseCase(setupRepository, outings),
            appDayClock,
        ),
    )
}

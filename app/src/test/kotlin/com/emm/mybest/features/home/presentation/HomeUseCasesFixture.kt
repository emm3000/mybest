package com.emm.mybest.features.home.presentation

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.usecase.checkin.ObserveCheckInTaskUseCase
import com.emm.mybest.domain.usecase.checkin.ObserveHasPendingCheckInUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveDailyChecksUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveWeekProgressUseCase
import com.emm.mybest.domain.usecase.dailycheck.ToggleDailyCheckUseCase
import com.emm.mybest.domain.usecase.home.ResolveHomeDestinationUseCase
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveTonightUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeSetupRepository

internal class HomeUseCasesFixture(
    private val dailyChecks: FakeDailyCheckRepository,
    private val outings: FakeOutingRepository,
    setup: Setup,
) {
    private val setupRepository = FakeSetupRepository(setup)

    fun build(
        appDayClock: AppDayClock,
        observeWeightProgress: ObserveWeightProgressUseCase,
        observePhotos: ObservePhotosUseCase,
    ): HomeUseCases = HomeUseCases(
        observeWeightProgress = observeWeightProgress,
        observePhotos = observePhotos,
        observeDailyChecks = ObserveDailyChecksUseCase(dailyChecks),
        observeWeekProgress = ObserveWeekProgressUseCase(dailyChecks, setupRepository),
        toggleDailyCheck = ToggleDailyCheckUseCase(dailyChecks),
        resolveDestination = ResolveHomeDestinationUseCase(
            ObserveCheckInTaskUseCase(outings, appDayClock),
            ObserveTonightUseCase(outings, appDayClock),
            appDayClock,
        ),
        observeHasPendingCheckIn = ObserveHasPendingCheckInUseCase(outings),
    )
}

package com.emm.mybest.features.home.presentation

import com.emm.mybest.domain.usecase.checkin.ObserveHasPendingCheckInUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveDailyChecksUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveWeekProgressUseCase
import com.emm.mybest.domain.usecase.dailycheck.ToggleDailyCheckUseCase
import com.emm.mybest.domain.usecase.home.ResolveHomeDestinationUseCase
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveNearestInstallmentUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase

data class HomeUseCases(
    val observeWeightProgress: ObserveWeightProgressUseCase,
    val observePhotos: ObservePhotosUseCase,
    val observeDailyChecks: ObserveDailyChecksUseCase,
    val observeWeekProgress: ObserveWeekProgressUseCase,
    val toggleDailyCheck: ToggleDailyCheckUseCase,
    val resolveDestination: ResolveHomeDestinationUseCase,
    val observeHasPendingCheckIn: ObserveHasPendingCheckInUseCase,
    val observeNextInstallment: ObserveNearestInstallmentUseCase,
)

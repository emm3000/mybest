package com.emm.mybest.domain.usecase.preouting

data class PreOutingDecisionUseCases(
    val observeTonight: ObserveTonightUseCase,
    val planOuting: PlanOutingUseCase,
    val recordWonNight: RecordWonNightUseCase,
)

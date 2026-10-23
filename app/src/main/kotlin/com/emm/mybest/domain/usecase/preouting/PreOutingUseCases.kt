package com.emm.mybest.domain.usecase.preouting

data class PreOutingUseCases(
    val outingMonth: ObserveOutingMonthUseCase,
    val nearestInstallment: ObserveNearestInstallmentUseCase,
    val purchaseGoalLeft: ObservePurchaseGoalLeftUseCase,
    val estimatedOutingCost: GetEstimatedOutingCostUseCase,
    val isEstimateAveraged: ObserveIsEstimateAveragedUseCase,
    val notSpentThisMonth: ObserveNotSpentThisMonthUseCase,
)

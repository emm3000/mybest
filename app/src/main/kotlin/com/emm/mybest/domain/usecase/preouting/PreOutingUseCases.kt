package com.emm.mybest.domain.usecase.preouting

data class PreOutingUseCases(
    val outingMonth: ObserveOutingMonthUseCase,
    val installmentInputs: ObserveInstallmentInputsUseCase,
    val purchaseGoalLeft: ObservePurchaseGoalLeftUseCase,
    val estimatedOutingCost: GetEstimatedOutingCostUseCase,
    val notSpentThisMonth: ObserveNotSpentThisMonthUseCase,
)

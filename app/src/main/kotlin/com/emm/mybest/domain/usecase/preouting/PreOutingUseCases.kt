package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase

data class PreOutingUseCases(
    val outingMonth: ObserveOutingMonthUseCase,
    val nearestInstallment: ObserveNearestInstallmentUseCase,
    val purchaseGoalLeft: ObservePurchaseGoalLeftUseCase,
    val estimatedOutingCost: GetEstimatedOutingCostUseCase,
    val notSpentThisMonth: ObserveNotSpentThisMonthUseCase,
    val debtSummary: ObserveDebtSummaryUseCase,
)

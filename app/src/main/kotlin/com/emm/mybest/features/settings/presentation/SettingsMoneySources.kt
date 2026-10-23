package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase

data class SettingsMoneySources(
    val observeDebtSummary: ObserveDebtSummaryUseCase,
    val purchaseGoalRepository: PurchaseGoalRepository,
)

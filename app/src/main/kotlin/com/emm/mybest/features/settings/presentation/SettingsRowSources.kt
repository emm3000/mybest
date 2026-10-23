package com.emm.mybest.features.settings.presentation

import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.usecase.capture.ObserveCaptureSummaryUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SettingsRowSources(
    private val observeDebtSummary: ObserveDebtSummaryUseCase,
    private val purchaseGoalRepository: PurchaseGoalRepository,
    private val observeCaptureSummary: ObserveCaptureSummaryUseCase,
) {

    fun observe(): Flow<SettingsRowsState> = combine(
        observeDebtSummary(),
        purchaseGoalRepository.observeGoal(),
        observeCaptureSummary(),
    ) { debtSummary, purchaseGoal, captureSummary ->
        SettingsRowsState(
            debts = SettingsDebtsState(
                count = debtSummary.count,
                totalLabel = debtSummary.totalBalance.formatPen().takeIf { debtSummary.count > 0 },
            ),
            purchaseGoal = SettingsPurchaseGoalState(
                name = purchaseGoal?.name,
                priceLabel = purchaseGoal?.price?.formatPen(),
            ),
            capture = SettingsCaptureState(
                isActive = captureSummary.isActive,
                selectedAppCount = captureSummary.selectedAppCount,
            ),
        )
    }
}

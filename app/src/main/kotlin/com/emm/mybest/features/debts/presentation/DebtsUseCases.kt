package com.emm.mybest.features.debts.presentation

import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.usecase.debts.MarkInstallmentPaidUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtListUseCase
import com.emm.mybest.domain.usecase.debts.SaveDebtUseCase

data class DebtsUseCases(
    val observeDebtList: ObserveDebtListUseCase,
    val saveDebt: SaveDebtUseCase,
    val markInstallmentPaid: MarkInstallmentPaidUseCase,
    val debtRepository: DebtRepository,
)

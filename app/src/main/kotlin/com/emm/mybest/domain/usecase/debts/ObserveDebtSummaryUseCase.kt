package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DebtSummary
import com.emm.mybest.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveDebtSummaryUseCase(
    private val debtRepository: DebtRepository,
) {

    /**
     * Emits how many debts exist and the sum of their balances.
     */
    operator fun invoke(): Flow<DebtSummary> = debtRepository.observeDebts().map { debts ->
        DebtSummary(count = debts.size, totalBalance = Money(debts.sumOf { it.balance.cents }))
    }
}

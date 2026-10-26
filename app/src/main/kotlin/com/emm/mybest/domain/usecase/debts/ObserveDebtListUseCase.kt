package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.DebtListItem
import com.emm.mybest.domain.models.hasPaymentFor
import com.emm.mybest.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class ObserveDebtListUseCase(
    private val debtRepository: DebtRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits every debt with whether the installment due on the current app day is already paid.
     */
    operator fun invoke(): Flow<List<DebtListItem>> = appDayClock.observeToday().flatMapLatest { today ->
        combine(debtRepository.observeDebts(), debtRepository.observePayments()) { debts, payments ->
            debts.map { DebtListItem(it, payments.hasPaymentFor(it, it.dueMonthOn(today))) }
        }
    }
}

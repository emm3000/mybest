package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.DebtListItem
import com.emm.mybest.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class ObserveDebtListUseCase(
    private val debtRepository: DebtRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits every debt with whether its installment is paid in the month of the current app day.
     */
    operator fun invoke(): Flow<List<DebtListItem>> = appDayClock.observeToday().flatMapLatest { today ->
        val month = YearMonthValue.from(today)
        combine(debtRepository.observeDebts(), debtRepository.observePayments()) { debts, payments ->
            val paidDebtIds = payments.filter { it.month == month }.map { it.debtId }.toSet()
            debts.map { DebtListItem(it, it.id in paidDebtIds) }
        }
    }
}

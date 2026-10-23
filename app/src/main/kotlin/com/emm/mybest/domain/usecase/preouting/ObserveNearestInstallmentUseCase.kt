package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.LocalDate

class ObserveNearestInstallmentUseCase(
    private val debtRepository: DebtRepository,
    private val getEstimatedOutingCost: GetEstimatedOutingCostUseCase,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits the unpaid installment with the earliest payment date on or after today, or null without debts. A debt
     * whose installment is paid this month, or whose date already passed, falls due next month; a payment day past
     * the end of a month falls on its last day.
     */
    operator fun invoke(): Flow<InstallmentShare?> = appDayClock.observeToday().flatMapLatest { today ->
        combine(
            debtRepository.observeDebts(),
            debtRepository.observePayments(),
            getEstimatedOutingCost(),
        ) { debts, payments, estimate ->
            nearestDue(debts, payments.paidDebtIds(YearMonthValue.from(today)), today)?.toShare(estimate.cost)
        }
    }
}

private fun List<DebtPayment>.paidDebtIds(month: YearMonthValue): Set<Long> =
    filter { it.month == month }.map { it.debtId }.toSet()

private fun nearestDue(debts: List<Debt>, paidDebtIds: Set<Long>, today: LocalDate): Pair<Debt, LocalDate>? =
    debts.map { it to nextPaymentDate(it, today, it.id in paidDebtIds) }.minByOrNull { it.second }

private fun Pair<Debt, LocalDate>.toShare(estimate: Money?): InstallmentShare =
    InstallmentShare(first.name, second, first.installment, estimate?.percentOf(first.installment))

private fun nextPaymentDate(debt: Debt, today: LocalDate, isPaidThisMonth: Boolean): LocalDate {
    val month = YearMonthValue.from(today)
    val thisMonth = debt.paymentDateIn(month)
    if (!isPaidThisMonth && thisMonth >= today) return thisMonth
    return debt.paymentDateIn(month.plusMonths(1))
}

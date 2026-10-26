package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.models.hasPaymentFor
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
     * Emits the installment with the earliest payment date on or after today among the debts with a balance, or null
     * without them. A debt's installment falls on its due month, or one month later when that month is already paid;
     * a payment day past the end of a month falls on its last day.
     */
    operator fun invoke(): Flow<InstallmentShare?> = appDayClock.observeToday().flatMapLatest { today ->
        combine(
            debtRepository.observeDebts(),
            debtRepository.observePayments(),
            getEstimatedOutingCost(),
        ) { debts, payments, estimate ->
            nearestDue(debts.filter { it.balance > Money.ZERO }, payments, today)?.toShare(estimate.cost)
        }
    }
}

private fun nearestDue(debts: List<Debt>, payments: List<DebtPayment>, today: LocalDate): Pair<Debt, LocalDate>? =
    debts.map { it to nextPaymentDate(it, payments, today) }.minByOrNull { it.second }

private fun Pair<Debt, LocalDate>.toShare(estimate: Money?): InstallmentShare =
    InstallmentShare(first.name, second, first.installment, estimate?.percentOf(first.installment))

private fun nextPaymentDate(debt: Debt, payments: List<DebtPayment>, today: LocalDate): LocalDate {
    val dueMonth = debt.dueMonthOn(today)
    return debt.paymentDateIn(if (payments.hasPaymentFor(debt, dueMonth)) dueMonth.plusMonths(1) else dueMonth)
}

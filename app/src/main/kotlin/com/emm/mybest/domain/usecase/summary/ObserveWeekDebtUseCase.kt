package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.DebtSummary
import com.emm.mybest.domain.models.WeekDebt
import com.emm.mybest.domain.models.debtIdsPaidIn
import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class ObserveWeekDebtUseCase(
    private val observeDebtSummary: ObserveDebtSummaryUseCase,
    private val debtRepository: DebtRepository,
) {

    /**
     * Emits the current debt balance, which is never rebuilt for a past week, with how many installments are paid in
     * the month of [appDay] against the number of debts.
     */
    operator fun invoke(appDay: LocalDate): Flow<WeekDebt> = combine(
        observeDebtSummary(),
        debtRepository.observePayments(),
    ) { summary, payments -> debtOf(summary, payments.debtIdsPaidIn(YearMonthValue.from(appDay)).size) }

    private fun debtOf(summary: DebtSummary, paidThisMonth: Int) =
        WeekDebt(summary.totalBalance, paidThisMonth, summary.count)
}

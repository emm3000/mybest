package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.repository.DebtRepository
import kotlinx.coroutines.flow.first

class MarkInstallmentPaidUseCase(
    private val debtRepository: DebtRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Marks the installment of the debt with [debtId] paid for the month it falls due on the current app day.
     *
     * @throws IllegalStateException when no debt has [debtId].
     */
    suspend operator fun invoke(debtId: Long) {
        val debt = checkNotNull(debtRepository.observeDebts().first().firstOrNull { it.id == debtId }) {
            "No debt with id $debtId"
        }
        debtRepository.markInstallmentPaid(debtId, debt.dueMonthOn(appDayClock.today()))
    }
}

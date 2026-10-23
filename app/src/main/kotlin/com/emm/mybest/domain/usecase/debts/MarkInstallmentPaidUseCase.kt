package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.repository.DebtRepository

class MarkInstallmentPaidUseCase(
    private val debtRepository: DebtRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Marks the installment of the debt with [debtId] paid for the month of the current app day.
     */
    suspend operator fun invoke(debtId: Long) {
        debtRepository.markInstallmentPaid(debtId, YearMonthValue.from(appDayClock.today()))
    }
}

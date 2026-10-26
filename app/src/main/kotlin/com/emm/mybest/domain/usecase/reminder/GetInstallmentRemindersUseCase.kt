package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.InstallmentReminder
import com.emm.mybest.domain.models.debtIdsPaidIn
import com.emm.mybest.domain.repository.DebtRepository
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

/**
 * How many days before its payment date an unpaid installment is reminded.
 */
const val DEBT_REMINDER_LEAD_DAYS = 3

/**
 * Decides which installments the debt reminder should notify about now.
 */
class GetInstallmentRemindersUseCase(
    private val debtRepository: DebtRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * With notifications on, the installments whose payment date is exactly [DEBT_REMINDER_LEAD_DAYS] after the
     * current app day, of debts with a balance left and still unpaid for that date's month; otherwise none.
     */
    suspend operator fun invoke(): List<InstallmentReminder> {
        if (!userPreferencesRepository.notificationsEnabled.first()) return emptyList()
        val dueDate = appDayClock.today().plus(DEBT_REMINDER_LEAD_DAYS, DateTimeUnit.DAY)
        val month = YearMonthValue.from(dueDate)
        val paidDebtIds = debtRepository.observePayments().first().debtIdsPaidIn(month)
        return debtRepository.observeDebts().first()
            .filter { it.hasBalance && it.id !in paidDebtIds }
            .filter { it.paymentDateIn(month) == dueDate }
            .map { InstallmentReminder(it, dueDate) }
    }
}

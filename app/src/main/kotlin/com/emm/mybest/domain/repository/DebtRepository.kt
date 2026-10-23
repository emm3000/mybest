package com.emm.mybest.domain.repository

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import kotlinx.coroutines.flow.Flow

/**
 * Stores debts and the installments paid on them, one payment per debt and month.
 */
interface DebtRepository {
    /**
     * Emits every debt, earliest payment day first.
     */
    fun observeDebts(): Flow<List<Debt>>

    /**
     * Emits every installment payment, oldest month first.
     */
    fun observePayments(): Flow<List<DebtPayment>>

    /**
     * Inserts [debt] when its id is 0, otherwise updates the stored debt, and returns its id.
     */
    suspend fun saveDebt(debt: Debt): Long

    /**
     * Removes the debt with [id] together with its payments.
     */
    suspend fun deleteDebt(id: Long)

    /**
     * Records this [month]'s installment of the debt with [debtId] as paid and lowers its balance with
     * [Debt.afterInstallmentPaid], both in one transaction. The payment amount is what the balance dropped by. When
     * that month is already paid, nothing changes.
     *
     * @throws IllegalStateException when no debt has [debtId].
     */
    suspend fun markInstallmentPaid(debtId: Long, month: YearMonthValue)
}

package com.emm.mybest.testing

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDebtRepository(
    initialDebts: List<Debt> = emptyList(),
    initialPayments: List<DebtPayment> = emptyList(),
) : DebtRepository {

    private val debts = MutableStateFlow(initialDebts)
    private val payments = MutableStateFlow(initialPayments)

    override fun observeDebts(): Flow<List<Debt>> = debts

    override fun observePayments(): Flow<List<DebtPayment>> = payments

    override suspend fun saveDebt(debt: Debt): Long {
        check(debt.id == 0L || debts.value.any { it.id == debt.id }) { "No debt with id ${debt.id}" }
        debts.value = debts.value.filterNot { it.id == debt.id } + debt
        return debt.id
    }

    override suspend fun deleteDebt(id: Long) {
        debts.value = debts.value.filterNot { it.id == id }
    }

    override suspend fun markInstallmentPaid(debtId: Long, month: YearMonthValue) {
        val debt = debts.value.first { it.id == debtId }
        val isAlreadyPaid = payments.value.any { it.debtId == debtId && it.month == month }
        if (isAlreadyPaid || !debt.hasBalance) return
        val paid = debt.afterInstallmentPaid()
        payments.value = payments.value + DebtPayment(debtId, month, debt.balance - paid.balance)
        debts.value = debts.value.map { if (it.id == debtId) paid else it }
    }
}

package com.emm.mybest.data

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.data.entities.DebtDao
import com.emm.mybest.data.mappers.toDomain
import com.emm.mybest.data.mappers.toEntity
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val NEW_DEBT_ID = 0L
private const val IGNORED_INSERT_ID = -1L

class DebtRepositoryImpl(
    private val debtDao: DebtDao,
) : DebtRepository {

    override fun observeDebts(): Flow<List<Debt>> = debtDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observePayments(): Flow<List<DebtPayment>> =
        debtDao.observePayments().map { list -> list.map { it.toDomain() } }

    override suspend fun saveDebt(debt: Debt): Long {
        if (debt.id == NEW_DEBT_ID) return debtDao.insert(debt.toEntity())
        checkNotNull(debtDao.findById(debt.id)) { "No debt with id ${debt.id}" }
        debtDao.update(debt.toEntity())
        return debt.id
    }

    override suspend fun deleteDebt(id: Long) {
        debtDao.deleteById(id)
    }

    override suspend fun markInstallmentPaid(debtId: Long, month: YearMonthValue) {
        debtDao.inTransaction { payInstallment(debtId, month) }
    }

    private suspend fun payInstallment(debtId: Long, month: YearMonthValue) {
        val debt = checkNotNull(debtDao.findById(debtId)) { "No debt with id $debtId" }.toDomain()
        if (!debt.hasBalance) return
        val paid = debt.afterInstallmentPaid()
        val payment = DebtPayment(debtId = debtId, month = month, amount = debt.balance - paid.balance)
        val isAlreadyPaid = debtDao.insertPayment(payment.toEntity()) == IGNORED_INSERT_ID
        if (isAlreadyPaid) return
        debtDao.update(paid.toEntity())
    }
}

package com.emm.mybest.data

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.data.entities.DebtDao
import com.emm.mybest.data.entities.DebtEntity
import com.emm.mybest.data.entities.DebtPaymentEntity
import com.emm.mybest.domain.models.Debt
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val OCTOBER = YearMonthValue(2026, 10)
private val STORED = DebtEntity(
    id = 2,
    name = "Tarjeta",
    balanceCents = 15_000,
    installmentCents = 20_000,
    paymentDay = 15,
)
private const val ALREADY_PAID = -1L

class DebtRepositoryImplTest {

    private val debtDao = mockk<DebtDao>(relaxUnitFun = true) {
        coEvery { inTransaction(any()) } coAnswers { firstArg<suspend () -> Unit>().invoke() }
        coEvery { findById(STORED.id) } returns STORED
    }
    private val repository = DebtRepositoryImpl(debtDao)

    @Test
    fun `marking an installment paid stores the payment and the lowered balance`() = runTest {
        val payment = slot<DebtPaymentEntity>()
        coEvery { debtDao.insertPayment(capture(payment)) } returns 1

        repository.markInstallmentPaid(STORED.id, OCTOBER)

        assertEquals(DebtPaymentEntity(debtId = 2, month = OCTOBER, amountCents = 15_000), payment.captured)
        coVerify { debtDao.update(STORED.copy(balanceCents = 0)) }
    }

    @Test
    fun `marking an already paid month leaves the balance unchanged`() = runTest {
        coEvery { debtDao.insertPayment(any()) } returns ALREADY_PAID

        repository.markInstallmentPaid(STORED.id, OCTOBER)

        coVerify(exactly = 0) { debtDao.update(any()) }
    }

    @Test
    fun `marking an installment paid on an unknown debt fails`() = runTest {
        coEvery { debtDao.findById(9) } returns null

        val failure = runCatching { repository.markInstallmentPaid(9, OCTOBER) }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
    }

    @Test
    fun `marking an installment paid on a settled debt stores no payment`() = runTest {
        coEvery { debtDao.findById(STORED.id) } returns STORED.copy(balanceCents = 0)

        repository.markInstallmentPaid(STORED.id, OCTOBER)

        coVerify(exactly = 0) { debtDao.insertPayment(any()) }
        coVerify(exactly = 0) { debtDao.update(any()) }
    }

    @Test
    fun `saving a debt with an unknown id fails`() = runTest {
        coEvery { debtDao.findById(9) } returns null

        val failure = runCatching {
            repository.saveDebt(Debt(9, "Fantasma", Money(10_000), Money(5_000), paymentDay = 3))
        }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
        coVerify(exactly = 0) { debtDao.update(any()) }
    }

    @Test
    fun `saving a debt with id 0 inserts it and returns the new id`() = runTest {
        coEvery { debtDao.insert(any()) } returns 5

        val id = repository.saveDebt(Debt(0, "Préstamo", Money(50_000), Money(20_000), paymentDay = 1))

        assertEquals(5L, id)
    }

    @Test
    fun `saving a stored debt updates it and returns its id`() = runTest {
        val id = repository.saveDebt(Debt(2, "Tarjeta", Money(10_000), Money(20_000), paymentDay = 15))

        assertEquals(2L, id)
        coVerify { debtDao.update(STORED.copy(balanceCents = 10_000)) }
        coVerify(exactly = 0) { debtDao.insert(any()) }
    }
}

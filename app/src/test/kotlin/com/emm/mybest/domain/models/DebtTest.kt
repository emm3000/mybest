package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private fun debt(balance: Money, installment: Money = Money(20_000)): Debt =
    Debt(id = 1, name = "Tarjeta", balance = balance, installment = installment, paymentDay = 15)

class DebtTest {

    @Test
    fun `paying a S 200 installment on a S 500 balance leaves S 300`() {
        assertEquals(Money(30_000), debt(balance = Money(50_000)).afterInstallmentPaid().balance)
    }

    @Test
    fun `paying a S 200 installment on a S 150 balance leaves it at zero`() {
        assertEquals(Money.ZERO, debt(balance = Money(15_000)).afterInstallmentPaid().balance)
    }

    @Test
    fun `paying an installment keeps the other debt fields`() {
        val debt = debt(balance = Money(50_000))

        assertEquals(debt.copy(balance = Money(30_000)), debt.afterInstallmentPaid())
    }

    @Test
    fun `a payment day outside 1 to 31 is rejected`() {
        val failures = listOf(0, 32).map { day -> runCatching { debt(Money.ZERO).copy(paymentDay = day) } }

        assertTrue(failures.all { it.exceptionOrNull() is IllegalArgumentException })
    }
}

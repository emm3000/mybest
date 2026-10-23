package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalDate
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

    @Test
    fun `a payment day 15 falls due on the 15th of the month`() {
        assertEquals(LocalDate(2026, 10, 15), debt(Money.ZERO).paymentDateIn(YearMonthValue(2026, 10)))
    }

    @Test
    fun `a payment day 31 falls due on the 30th of a 30-day month`() {
        val debt = debt(Money.ZERO).copy(paymentDay = 31)

        assertEquals(LocalDate(2026, 9, 30), debt.paymentDateIn(YearMonthValue(2026, 9)))
    }
}

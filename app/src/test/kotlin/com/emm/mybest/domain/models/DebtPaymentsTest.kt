package com.emm.mybest.domain.models

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Test

private val OCTOBER = YearMonthValue(2026, 10)
private val SEPTEMBER = YearMonthValue(2026, 9)

class DebtPaymentsTest {

    @Test
    fun `gives the debts paid in the month`() {
        val payments = listOf(
            DebtPayment(1, OCTOBER, Money(100_00)),
            DebtPayment(2, SEPTEMBER, Money(100_00)),
            DebtPayment(3, OCTOBER, Money(50_00)),
        )

        assertEquals(setOf(1L, 3L), payments.debtIdsPaidIn(OCTOBER))
    }

    @Test
    fun `gives no debts without payments in the month`() {
        assertEquals(emptySet<Long>(), listOf(DebtPayment(1, SEPTEMBER, Money(100_00))).debtIdsPaidIn(OCTOBER))
    }
}

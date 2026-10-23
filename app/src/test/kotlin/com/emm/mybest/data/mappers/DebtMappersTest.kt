package com.emm.mybest.data.mappers

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import org.junit.Assert.assertEquals
import org.junit.Test

class DebtMappersTest {

    @Test
    fun `a debt round-trips through the entity`() {
        val debt =
            Debt(id = 3, name = "Préstamo", balance = Money(50_000), installment = Money(20_000), paymentDay = 31)

        assertEquals(debt, debt.toEntity().toDomain())
    }

    @Test
    fun `a debt payment round-trips through the entity`() {
        val payment = DebtPayment(debtId = 3, month = YearMonthValue(2026, 10), amount = Money(20_000))

        assertEquals(payment, payment.toEntity().toDomain())
    }
}

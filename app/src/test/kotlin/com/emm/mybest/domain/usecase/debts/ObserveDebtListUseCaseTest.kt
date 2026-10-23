package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtListItem
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val CARD = Debt(1, "Tarjeta", Money(320_000), Money(42_000), paymentDay = 15)
private val MOTO = Debt(2, "Moto", Money(215_000), Money(31_000), paymentDay = 5)
private val OCTOBER = YearMonthValue(2026, 10)

class ObserveDebtListUseCaseTest {

    private val clock = AppDayClock(FixedClock(LocalDateTime(2026, 10, 23, 12, 0).toInstant(LIMA)), LIMA)

    private fun useCase(payments: List<DebtPayment>) =
        ObserveDebtListUseCase(FakeDebtRepository(listOf(CARD, MOTO), payments), clock)

    @Test
    fun `flags only the debts paid in the current month`() = runTest {
        val items = useCase(listOf(DebtPayment(MOTO.id, OCTOBER, MOTO.installment)))().first()

        assertEquals(listOf(DebtListItem(CARD, false), DebtListItem(MOTO, true)), items)
    }

    @Test
    fun `ignores payments of other months`() = runTest {
        val september = DebtPayment(CARD.id, OCTOBER.minusMonths(1), CARD.installment)

        assertEquals(listOf(false, false), useCase(listOf(september))().first().map { it.isPaidThisMonth })
    }
}

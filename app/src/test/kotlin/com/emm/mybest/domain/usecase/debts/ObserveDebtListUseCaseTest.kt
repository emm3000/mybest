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
private val CARD = Debt(1, "Tarjeta", Money(100_000), Money(20_000), paymentDay = 2)
private val BANK = Debt(2, "Banco", Money(160_000), Money(40_000), paymentDay = 25)
private val NOVEMBER = YearMonthValue(2026, 11)
private val DECEMBER = YearMonthValue(2026, 12)

class ObserveDebtListUseCaseTest {

    private fun clockAt(dateTime: LocalDateTime) = AppDayClock(FixedClock(dateTime.toInstant(LIMA)), LIMA)

    private suspend fun items(payments: List<DebtPayment>, now: LocalDateTime): List<DebtListItem> =
        ObserveDebtListUseCase(FakeDebtRepository(listOf(CARD, BANK), payments), clockAt(now))().first()

    private suspend fun isCardPaid(payments: List<DebtPayment>, now: LocalDateTime): Boolean =
        items(payments, now).first { it.debt == CARD }.isDueInstallmentPaid

    @Test
    fun `flags only the debts whose due installment is paid`() = runTest {
        val payments = listOf(DebtPayment(BANK.id, NOVEMBER, BANK.installment))

        val items = items(payments, LocalDateTime(2026, 11, 10, 10, 0))

        assertEquals(listOf(DebtListItem(CARD, false), DebtListItem(BANK, true)), items)
    }

    @Test
    fun `an early payment for next month shows paid before the new month starts`() = runTest {
        val payments = listOf(DebtPayment(CARD.id, DECEMBER, CARD.installment))

        assertEquals(true, isCardPaid(payments, LocalDateTime(2026, 11, 30, 10, 0)))
    }

    @Test
    fun `an early payment for next month still shows paid on the first of that month`() = runTest {
        val payments = listOf(DebtPayment(CARD.id, DECEMBER, CARD.installment))

        assertEquals(true, isCardPaid(payments, LocalDateTime(2026, 12, 1, 10, 0)))
    }

    @Test
    fun `a payment stops counting once its payment date has passed`() = runTest {
        val payments = listOf(DebtPayment(CARD.id, DECEMBER, CARD.installment))

        assertEquals(false, isCardPaid(payments, LocalDateTime(2026, 12, 3, 10, 0)))
    }

    @Test
    fun `a new month asks for the installment again`() = runTest {
        val payments = listOf(DebtPayment(BANK.id, NOVEMBER, BANK.installment))

        val bankOnDecember = items(payments, LocalDateTime(2026, 12, 1, 10, 0)).first { it.debt == BANK }

        assertEquals(false, bankOnDecember.isDueInstallmentPaid)
    }

    @Test
    fun `ignores payments of other months`() = runTest {
        val october = DebtPayment(CARD.id, NOVEMBER.minusMonths(1), CARD.installment)

        assertEquals(false, isCardPaid(listOf(october), LocalDateTime(2026, 11, 10, 10, 0)))
    }
}

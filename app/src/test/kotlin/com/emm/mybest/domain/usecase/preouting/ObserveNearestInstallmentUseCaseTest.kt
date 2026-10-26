package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val NOVEMBER = YearMonthValue(2026, 11)
private val DECEMBER = YearMonthValue(2026, 12)

class ObserveNearestInstallmentUseCaseTest {

    private fun debt(id: Long, name: String, paymentDay: Int, balance: Long = 1_600_00) =
        Debt(id, name, Money(balance), Money(400_00), paymentDay)

    private suspend fun nearest(
        debts: List<Debt>,
        today: LocalDate,
        payments: List<DebtPayment> = emptyList(),
    ): InstallmentShare? {
        val clock = AppDayClock(FixedClock(LocalDateTime(today, LocalTime(12, 0)).toInstant(LIMA)), LIMA)
        val estimate = GetEstimatedOutingCostUseCase(FakeSetupRepository(Setup()), FakeOutingRepository())
        return ObserveNearestInstallmentUseCase(FakeDebtRepository(debts, payments), estimate, clock)().first()
    }

    private fun paid(debt: Debt, month: YearMonthValue) = DebtPayment(debt.id, month, debt.installment)

    @Test
    fun `skips a paid-off debt even when its date is the earliest`() = runTest {
        val paidOff = debt(1, "Parcial", paymentDay = 12, balance = 0)
        val bank = debt(2, "Banco", paymentDay = 25)

        val share = nearest(listOf(paidOff, bank), LocalDate(2026, 11, 10))

        assertEquals("Banco", share?.debtName)
        assertEquals(LocalDate(2026, 11, 25), share?.paymentDate)
    }

    @Test
    fun `emits null when every debt is paid off`() = runTest {
        val debts =
            listOf(debt(1, "Parcial", paymentDay = 12, balance = 0), debt(2, "Moto", paymentDay = 3, balance = 0))

        assertNull(nearest(debts, LocalDate(2026, 11, 10)))
    }

    @Test
    fun `emits null without debts`() = runTest {
        assertNull(nearest(emptyList(), LocalDate(2026, 11, 10)))
    }

    @Test
    fun `an installment paid early for next month moves the date one month later`() = runTest {
        val card = debt(1, "Tarjeta", paymentDay = 2)

        val share = nearest(listOf(card), LocalDate(2026, 11, 30), listOf(paid(card, DECEMBER)))

        assertEquals(LocalDate(2027, 1, 2), share?.paymentDate)
    }

    @Test
    fun `an installment paid early still moves the date on the first of that month`() = runTest {
        val card = debt(1, "Tarjeta", paymentDay = 2)

        val share = nearest(listOf(card), LocalDate(2026, 12, 1), listOf(paid(card, DECEMBER)))

        assertEquals(LocalDate(2027, 1, 2), share?.paymentDate)
    }

    @Test
    fun `an unpaid installment falls on its due month`() = runTest {
        val card = debt(1, "Tarjeta", paymentDay = 2)

        assertEquals(LocalDate(2026, 12, 2), nearest(listOf(card), LocalDate(2026, 11, 30))?.paymentDate)
    }

    @Test
    fun `picks the earliest of several unpaid debts`() = runTest {
        val early = debt(1, "Alquiler", paymentDay = 5)
        val late = debt(2, "Banco", paymentDay = 20)

        assertEquals(LocalDate(2026, 11, 20), nearest(listOf(early, late), LocalDate(2026, 11, 10))?.paymentDate)
    }

    @Test
    fun `after the nearest debt is paid the next one wins`() = runTest {
        val early = debt(1, "Alquiler", paymentDay = 5)
        val late = debt(2, "Banco", paymentDay = 20)

        val share = nearest(listOf(early, late), LocalDate(2026, 11, 10), listOf(paid(late, NOVEMBER)))

        assertEquals(LocalDate(2026, 12, 5), share?.paymentDate)
    }
}

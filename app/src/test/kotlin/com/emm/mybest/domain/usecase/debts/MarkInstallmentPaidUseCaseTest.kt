package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MutableClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")

class MarkInstallmentPaidUseCaseTest {

    private fun debt(paymentDay: Int) = Debt(1, "Tarjeta", Money(100_000), Money(20_000), paymentDay)

    private fun clockAt(dateTime: LocalDateTime) = AppDayClock(FixedClock(dateTime.toInstant(LIMA)), LIMA)

    private suspend fun bookedMonths(paymentDay: Int, now: LocalDateTime): List<YearMonthValue> {
        val repository = FakeDebtRepository(listOf(debt(paymentDay)))
        MarkInstallmentPaidUseCase(repository, clockAt(now))(1)
        return repository.observePayments().first().map { it.month }
    }

    @Test
    fun `books to the next month when the payment day already passed`() = runTest {
        assertEquals(listOf(YearMonthValue(2026, 12)), bookedMonths(2, LocalDateTime(2026, 11, 30, 10, 0)))
    }

    @Test
    fun `books to the current month when the payment day is still ahead`() = runTest {
        assertEquals(listOf(YearMonthValue(2026, 11)), bookedMonths(15, LocalDateTime(2026, 11, 10, 10, 0)))
    }

    @Test
    fun `books to the current month when the payment day is today`() = runTest {
        assertEquals(listOf(YearMonthValue(2026, 11)), bookedMonths(2, LocalDateTime(2026, 11, 2, 10, 0)))
    }

    @Test
    fun `books a day 31 debt to a 30 day month on its last day`() = runTest {
        assertEquals(listOf(YearMonthValue(2026, 11)), bookedMonths(31, LocalDateTime(2026, 11, 30, 10, 0)))
    }

    @Test
    fun `before 05 00 the app day still belongs to the previous day`() = runTest {
        assertEquals(listOf(YearMonthValue(2026, 11)), bookedMonths(31, LocalDateTime(2026, 12, 1, 3, 0)))
    }

    @Test
    fun `paying again on the same or the next app day never deducts twice`() = runTest {
        val repository = FakeDebtRepository(listOf(debt(2).copy(balance = Money(100_000))))
        val clock = MutableClock(LocalDateTime(2026, 11, 30, 10, 0).toInstant(LIMA))
        val useCase = MarkInstallmentPaidUseCase(repository, AppDayClock(clock, LIMA))

        useCase(1)
        useCase(1)
        clock.instant = LocalDateTime(2026, 12, 1, 10, 0).toInstant(LIMA)
        useCase(1)

        assertEquals(1, repository.observePayments().first().size)
        assertEquals(listOf(Money(80_000)), repository.observeDebts().first().map { it.balance })
    }
}

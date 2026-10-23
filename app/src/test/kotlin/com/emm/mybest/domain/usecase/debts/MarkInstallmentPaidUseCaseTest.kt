package com.emm.mybest.domain.usecase.debts

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Debt
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

class MarkInstallmentPaidUseCaseTest {

    private fun clockAt(dateTime: LocalDateTime) = AppDayClock(FixedClock(dateTime.toInstant(LIMA)), LIMA)

    @Test
    fun `pays the installment for the month of the current app day`() = runTest {
        val repository = FakeDebtRepository(listOf(CARD))
        val useCase = MarkInstallmentPaidUseCase(repository, clockAt(LocalDateTime(2026, 10, 23, 12, 0)))

        useCase(CARD.id)

        assertEquals(listOf(YearMonthValue(2026, 10)), repository.observePayments().first().map { it.month })
    }

    @Test
    fun `before 05 00 the app day still belongs to the previous month`() = runTest {
        val repository = FakeDebtRepository(listOf(CARD))
        val useCase = MarkInstallmentPaidUseCase(repository, clockAt(LocalDateTime(2026, 11, 1, 3, 0)))

        useCase(CARD.id)

        assertEquals(listOf(YearMonthValue(2026, 10)), repository.observePayments().first().map { it.month })
    }
}

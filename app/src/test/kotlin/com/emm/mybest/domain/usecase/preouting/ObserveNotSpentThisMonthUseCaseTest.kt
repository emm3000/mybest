package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val ESTIMATE = Money(380_00)

class ObserveNotSpentThisMonthUseCaseTest {

    private val clock = AppDayClock(FixedClock(LocalDateTime(2026, 10, 23, 12, 0).toInstant(LIMA)), LIMA)

    private fun useCase(wonNights: List<WonNight>) = ObserveNotSpentThisMonthUseCase(
        outingRepository = FakeOutingRepository(initialWonNights = wonNights),
        appDayClock = clock,
    )

    @Test
    fun `is zero without won nights`() = runTest {
        assertEquals(Money.ZERO, useCase(emptyList())().first())
    }

    @Test
    fun `sums the estimated cost of this month's won nights`() = runTest {
        val wonNights = listOf(WonNight(LocalDate(2026, 10, 9), ESTIMATE), WonNight(FRIDAY, Money(100_00)))

        assertEquals(Money(480_00), useCase(wonNights)().first())
    }

    @Test
    fun `counts a won night without an estimate as zero`() = runTest {
        val wonNights = listOf(WonNight(LocalDate(2026, 10, 9), estimatedCost = null), WonNight(FRIDAY, ESTIMATE))

        assertEquals(ESTIMATE, useCase(wonNights)().first())
    }

    @Test
    fun `excludes a won night of the previous month`() = runTest {
        val wonNights = listOf(WonNight(LocalDate(2026, 9, 25), ESTIMATE), WonNight(FRIDAY, ESTIMATE))

        assertEquals(ESTIMATE, useCase(wonNights)().first())
    }

    @Test
    fun `includes a won night on the last day of the month`() = runTest {
        val wonNights = listOf(WonNight(LocalDate(2026, 10, 31), ESTIMATE), WonNight(LocalDate(2026, 11, 1), ESTIMATE))

        assertEquals(ESTIMATE, useCase(wonNights)().first())
    }
}

package com.emm.mybest.domain.usecase.home

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.usecase.checkin.ObserveCheckInTaskUseCase
import com.emm.mybest.domain.usecase.preouting.ObserveTonightUseCase
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val WEDNESDAY = LocalDate(2026, 10, 21)
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val SUNDAY = LocalDate(2026, 10, 25)
private val MONDAY = LocalDate(2026, 10, 26)
private val LIMITS = Limits(Money(150_00), drinks = 4, returnTime = LocalTime(1, 30))

class ResolveHomeDestinationUseCaseTest {

    private suspend fun destinationAt(
        date: LocalDate,
        hour: Int,
        repository: FakeOutingRepository = FakeOutingRepository(),
    ): HomeDestination {
        val clock = AppDayClock(FixedClock(LocalDateTime(date, LocalTime(hour, 0)).toInstant(LIMA)), LIMA)
        val useCase = ResolveHomeDestinationUseCase(
            observeCheckInTask = ObserveCheckInTaskUseCase(repository, clock),
            observeTonight = ObserveTonightUseCase(repository, clock),
            appDayClock = clock,
        )
        return useCase().first()
    }

    private fun plannedOn(appDay: LocalDate) = Outing(1, appDay, OutingKind.PLANNED, LIMITS, checkIn = null)

    @Test
    fun `friday from 18_00 opens the pre-outing`() = runTest {
        assertEquals(HomeDestination.PRE_OUTING, destinationAt(FRIDAY, 18))
    }

    @Test
    fun `saturday evening opens the pre-outing`() = runTest {
        assertEquals(HomeDestination.PRE_OUTING, destinationAt(SATURDAY, 20))
    }

    @Test
    fun `saturday at 02_00 is still friday evening`() = runTest {
        assertEquals(HomeDestination.PRE_OUTING, destinationAt(SATURDAY, 2))
    }

    @Test
    fun `friday before 18_00 shows the daily checks`() = runTest {
        assertEquals(HomeDestination.DAILY_CHECKS, destinationAt(FRIDAY, 17))
    }

    @Test
    fun `an outing already planned for the evening shows the daily checks`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(plannedOn(FRIDAY)))

        assertEquals(HomeDestination.DAILY_CHECKS, destinationAt(FRIDAY, 20, repository))
    }

    @Test
    fun `a won night already recorded shows the daily checks`() = runTest {
        val repository = FakeOutingRepository(initialWonNights = listOf(WonNight(FRIDAY, Money(380_00))))

        assertEquals(HomeDestination.DAILY_CHECKS, destinationAt(FRIDAY, 20, repository))
    }

    @Test
    fun `a morning with a pending outing opens the check-in`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(plannedOn(FRIDAY)))

        assertEquals(HomeDestination.CHECK_IN, destinationAt(SATURDAY, 9, repository))
    }

    @Test
    fun `a weekend morning with no outing recorded opens the check-in`() = runTest {
        assertEquals(HomeDestination.CHECK_IN, destinationAt(SUNDAY, 9))
    }

    @Test
    fun `a weekday morning without a task shows the daily checks`() = runTest {
        assertEquals(HomeDestination.DAILY_CHECKS, destinationAt(WEDNESDAY, 9))
    }

    @Test
    fun `from noon a pending outing no longer opens the check-in`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(plannedOn(FRIDAY)))

        assertEquals(HomeDestination.DAILY_CHECKS, destinationAt(SATURDAY, 12, repository))
    }

    @Test
    fun `sunday from 18_00 opens the weekly summary`() = runTest {
        assertEquals(HomeDestination.WEEKLY_SUMMARY, destinationAt(SUNDAY, 18))
    }

    @Test
    fun `monday at 02_00 is still sunday evening`() = runTest {
        assertEquals(HomeDestination.WEEKLY_SUMMARY, destinationAt(MONDAY, 2))
    }

    @Test
    fun `sunday before 18_00 shows the daily checks`() = runTest {
        assertEquals(HomeDestination.DAILY_CHECKS, destinationAt(SUNDAY, 15))
    }

    @Test
    fun `a weekday afternoon shows the daily checks`() = runTest {
        assertEquals(HomeDestination.DAILY_CHECKS, destinationAt(WEDNESDAY, 15))
    }
}

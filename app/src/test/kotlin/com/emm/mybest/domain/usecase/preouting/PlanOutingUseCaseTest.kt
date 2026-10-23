package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WonNight
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
import org.junit.Assert.assertTrue
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val LIMITS = Limits(money = Money(150_00), drinks = 4, returnTime = LocalTime(1, 30))

class PlanOutingUseCaseTest {

    private fun useCase(
        repository: FakeOutingRepository,
        now: LocalDateTime = LocalDateTime(
            FRIDAY,
            LocalTime(21, 0),
        ),
    ) =
        PlanOutingUseCase(repository, AppDayClock(FixedClock(now.toInstant(LIMA)), LIMA))

    private fun existingOuting() = Outing(1, FRIDAY, OutingKind.UNPLANNED, limits = null, checkIn = null)

    @Test
    fun `records a planned outing for the current app day`() = runTest {
        val repository = FakeOutingRepository()

        val result = useCase(repository)(LIMITS)

        assertEquals(PlanOutingResult.Planned, result)
        val outing = repository.observeAll().first().single()
        assertEquals(FRIDAY, outing.appDay)
        assertEquals(OutingKind.PLANNED, outing.kind)
        assertEquals(LIMITS, outing.limits)
    }

    @Test
    fun `records on the previous calendar day before the app day starts`() = runTest {
        val repository = FakeOutingRepository()

        useCase(repository, LocalDateTime(LocalDate(2026, 10, 24), LocalTime(2, 0)))(LIMITS)

        assertEquals(FRIDAY, repository.observeAll().first().single().appDay)
    }

    @Test
    fun `rejects a money limit of zero`() = runTest {
        val repository = FakeOutingRepository()

        val result = useCase(repository)(LIMITS.copy(money = Money.ZERO))

        assertEquals(PlanOutingResult.NonPositiveMoney, result)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `rejects a negative money limit`() = runTest {
        val result = useCase(FakeOutingRepository())(LIMITS.copy(money = Money(-1)))

        assertEquals(PlanOutingResult.NonPositiveMoney, result)
    }

    @Test
    fun `rejects negative drinks`() = runTest {
        val repository = FakeOutingRepository()

        val result = useCase(repository)(LIMITS.copy(drinks = -1))

        assertEquals(PlanOutingResult.NegativeDrinks, result)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `accepts zero drinks`() = runTest {
        val result = useCase(FakeOutingRepository())(LIMITS.copy(drinks = 0))

        assertEquals(PlanOutingResult.Planned, result)
    }

    @Test
    fun `rejects an app day that already has an outing`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(existingOuting()))

        val result = useCase(repository)(LIMITS)

        assertEquals(PlanOutingResult.DayAlreadyDecided, result)
        assertEquals(1, repository.observeAll().first().size)
    }

    @Test
    fun `rejects an app day that already has a won night`() = runTest {
        val repository = FakeOutingRepository(initialWonNights = listOf(WonNight(FRIDAY, estimatedCost = null)))

        val result = useCase(repository)(LIMITS)

        assertEquals(PlanOutingResult.DayAlreadyDecided, result)
        assertTrue(repository.observeAll().first().isEmpty())
    }
}

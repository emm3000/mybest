package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.NightDecision
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val THURSDAY = LocalDate(2026, 10, 22)
private val LIMITS = Limits(Money(150_00), drinks = 4, returnTime = LocalTime(1, 30))

class ObserveTonightUseCaseTest {

    private fun useCase(repository: FakeOutingRepository, today: LocalDate = FRIDAY) = ObserveTonightUseCase(
        repository,
        AppDayClock(FixedClock(LocalDateTime(today, LocalTime(21, 0)).toInstant(LIMA)), LIMA),
    )

    @Test
    fun `is undecided without an outing or won night on the app day`() = runTest {
        val tonight = useCase(FakeOutingRepository()).invoke().first()

        assertEquals(FRIDAY, tonight.appDay)
        assertEquals(NightDecision.Undecided, tonight.decision)
    }

    @Test
    fun `shows the outing recorded on the app day`() = runTest {
        val outing = Outing(1, FRIDAY, OutingKind.PLANNED, LIMITS, checkIn = null)

        val tonight = useCase(FakeOutingRepository(initialOutings = listOf(outing))).invoke().first()

        assertEquals(NightDecision.WentOut(outing), tonight.decision)
    }

    @Test
    fun `shows the won night recorded on the app day`() = runTest {
        val wonNight = WonNight(FRIDAY, Money(380_00))

        val tonight = useCase(FakeOutingRepository(initialWonNights = listOf(wonNight))).invoke().first()

        assertEquals(NightDecision.StayedIn(wonNight), tonight.decision)
    }

    @Test
    fun `ignores decisions from other app days`() = runTest {
        val yesterday = Outing(1, THURSDAY, OutingKind.PLANNED, LIMITS, checkIn = null)

        val tonight = useCase(FakeOutingRepository(initialOutings = listOf(yesterday))).invoke().first()

        assertEquals(NightDecision.Undecided, tonight.decision)
    }

    @Test
    fun `can stay in only on Friday and Saturday`() = runTest {
        assertTrue(useCase(FakeOutingRepository(), FRIDAY).invoke().first().canStayIn)
        assertFalse(useCase(FakeOutingRepository(), THURSDAY).invoke().first().canStayIn)
    }

    @Test
    fun `reacts when the user records a decision`() = runTest {
        val repository = FakeOutingRepository()
        val useCase = useCase(repository)

        repository.recordWonNight(WonNight(FRIDAY, estimatedCost = null))

        assertEquals(NightDecision.StayedIn(WonNight(FRIDAY, null)), useCase().first().decision)
    }
}

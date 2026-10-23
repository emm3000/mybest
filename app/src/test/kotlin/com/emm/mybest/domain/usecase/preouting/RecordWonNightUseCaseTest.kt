package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WonNight
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
import org.junit.Assert.assertTrue
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val THURSDAY = LocalDate(2026, 10, 22)
private val TYPICAL_COST = Money(380_00)

class RecordWonNightUseCaseTest {

    private fun useCase(
        repository: FakeOutingRepository,
        now: LocalDateTime = LocalDateTime(FRIDAY, LocalTime(21, 0)),
        setup: Setup = Setup(typicalOutingCost = TYPICAL_COST),
    ) = RecordWonNightUseCase(
        outingRepository = repository,
        getEstimatedOutingCost = GetEstimatedOutingCostUseCase(FakeSetupRepository(setup), repository),
        appDayClock = AppDayClock(FixedClock(now.toInstant(LIMA)), LIMA),
    )

    private suspend fun FakeOutingRepository.wonNights() = observeWonNights(THURSDAY, SATURDAY).first()

    @Test
    fun `records a won night on Friday with the estimate at that moment`() = runTest {
        val repository = FakeOutingRepository()

        val result = useCase(repository)()

        assertEquals(RecordWonNightResult.Recorded(TYPICAL_COST), result)
        assertEquals(listOf(WonNight(FRIDAY, TYPICAL_COST)), repository.wonNights())
    }

    @Test
    fun `records a won night on Saturday`() = runTest {
        val repository = FakeOutingRepository()

        val result = useCase(repository, LocalDateTime(SATURDAY, LocalTime(20, 0)))()

        assertEquals(RecordWonNightResult.Recorded(TYPICAL_COST), result)
        assertEquals(SATURDAY, repository.wonNights().single().appDay)
    }

    @Test
    fun `records a won night after midnight on the previous app day`() = runTest {
        val repository = FakeOutingRepository()

        useCase(repository, LocalDateTime(SATURDAY, LocalTime(2, 0)))()

        assertEquals(FRIDAY, repository.wonNights().single().appDay)
    }

    @Test
    fun `stores no estimate when none is known`() = runTest {
        val repository = FakeOutingRepository()

        val result = useCase(repository, setup = Setup())()

        assertEquals(RecordWonNightResult.Recorded(estimatedCost = null), result)
        assertEquals(listOf(WonNight(FRIDAY, estimatedCost = null)), repository.wonNights())
    }

    @Test
    fun `rejects an app day that is not Friday or Saturday`() = runTest {
        val repository = FakeOutingRepository()

        val result = useCase(repository, LocalDateTime(THURSDAY, LocalTime(21, 0)))()

        assertEquals(RecordWonNightResult.NotWonNightDay, result)
        assertTrue(repository.wonNights().isEmpty())
    }

    @Test
    fun `records after midnight on Saturday night on the Saturday app day`() = runTest {
        val sundayEarly = LocalDateTime(LocalDate(2026, 10, 25), LocalTime(2, 0))

        val result = useCase(FakeOutingRepository(), sundayEarly)()

        assertTrue(result is RecordWonNightResult.Recorded)
    }

    @Test
    fun `rejects an app day that already has an outing`() = runTest {
        val outing = Outing(1, FRIDAY, OutingKind.UNPLANNED, limits = null, checkIn = null)
        val repository = FakeOutingRepository(initialOutings = listOf(outing))

        val result = useCase(repository)()

        assertEquals(RecordWonNightResult.DayAlreadyDecided, result)
        assertTrue(repository.wonNights().isEmpty())
    }

    @Test
    fun `rejects an app day that already has a won night`() = runTest {
        val earlier = WonNight(FRIDAY, Money(100_00))
        val repository = FakeOutingRepository(initialWonNights = listOf(earlier))

        val result = useCase(repository)()

        assertEquals(RecordWonNightResult.DayAlreadyDecided, result)
        assertEquals(listOf(earlier), repository.wonNights())
    }
}

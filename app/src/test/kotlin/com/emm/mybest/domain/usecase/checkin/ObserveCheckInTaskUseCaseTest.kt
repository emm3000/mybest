package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
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
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val WEDNESDAY = LocalDate(2026, 10, 28)
private val EARLIER_FRIDAY = LocalDate(2026, 10, 16)
private val LIMITS = Limits(Money(150_00), drinks = 4, returnTime = LocalTime(1, 30))
private val MORNING = LocalTime(9, 0)

class ObserveCheckInTaskUseCaseTest {

    private fun useCase(repository: FakeOutingRepository, today: LocalDate) = ObserveCheckInTaskUseCase(
        repository,
        AppDayClock(FixedClock(LocalDateTime(today, MORNING).toInstant(LIMA)), LIMA),
    )

    private fun plannedOn(appDay: LocalDate, id: Long = 1) =
        Outing(id, appDay, OutingKind.PLANNED, LIMITS, checkIn = null)

    @Test
    fun `asks for the check-in of a pending outing from the evening before`() = runTest {
        val outing = plannedOn(FRIDAY)

        val task = useCase(FakeOutingRepository(initialOutings = listOf(outing)), SATURDAY).invoke().first()

        assertEquals(CheckInTask.PendingOuting(outing), task)
    }

    @Test
    fun `asks if the user went out on a weekend morning with no outing the evening before`() = runTest {
        val task = useCase(FakeOutingRepository(), SATURDAY).invoke().first()

        assertEquals(CheckInTask.AskUnplanned(FRIDAY), task)
    }

    @Test
    fun `still asks when the evening has only a won night`() = runTest {
        val repository = FakeOutingRepository(initialWonNights = listOf(WonNight(FRIDAY, Money(380_00))))

        val task = useCase(repository, SATURDAY).invoke().first()

        assertEquals(CheckInTask.AskUnplanned(FRIDAY), task)
    }

    @Test
    fun `does not ask again once the user answered no for the evening`() = runTest {
        val repository = FakeOutingRepository(initialNoOutingAnswers = setOf(FRIDAY))

        val task = useCase(repository, SATURDAY).invoke().first()

        assertEquals(CheckInTask.NoTask, task)
    }

    @Test
    fun `has no task on a weekday with no outings`() = runTest {
        val task = useCase(FakeOutingRepository(), WEDNESDAY).invoke().first()

        assertEquals(CheckInTask.NoTask, task)
    }

    @Test
    fun `puts a pending outing from an earlier week before the question`() = runTest {
        val outing = plannedOn(EARLIER_FRIDAY)

        val task = useCase(FakeOutingRepository(initialOutings = listOf(outing)), SATURDAY).invoke().first()

        assertEquals(CheckInTask.PendingOuting(outing), task)
    }

    @Test
    fun `does not ask when the evening already has a completed outing`() = runTest {
        val done = plannedOn(FRIDAY).copy(checkIn = CheckIn(Money(90_00), 2, LocalTime(1, 0), mood = 4))

        val task = useCase(FakeOutingRepository(initialOutings = listOf(done)), SATURDAY).invoke().first()

        assertEquals(CheckInTask.NoTask, task)
    }

    @Test
    fun `does not offer the outing of the app day still in progress`() = runTest {
        val tonight = plannedOn(WEDNESDAY)

        val task = useCase(FakeOutingRepository(initialOutings = listOf(tonight)), WEDNESDAY).invoke().first()

        assertEquals(CheckInTask.NoTask, task)
    }
}

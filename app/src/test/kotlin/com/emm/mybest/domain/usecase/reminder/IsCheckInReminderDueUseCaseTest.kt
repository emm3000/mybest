package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeUserPreferencesRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val THURSDAY = LocalDate(2026, 10, 22)
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val LIMITS = Limits(Money(150_00), drinks = 4, returnTime = LocalTime(1, 30))
private val MID_MORNING = LocalTime(10, 0)

class IsCheckInReminderDueUseCaseTest {

    private fun useCase(
        repository: FakeOutingRepository,
        today: LocalDate,
        preferences: FakeUserPreferencesRepository = FakeUserPreferencesRepository(),
    ) = IsCheckInReminderDueUseCase(
        preferences,
        repository,
        AppDayClock(FixedClock(LocalDateTime(today, MID_MORNING).toInstant(LIMA)), LIMA),
    )

    private fun plannedOn(appDay: LocalDate) = Outing(1, appDay, OutingKind.PLANNED, LIMITS, checkIn = null)

    @Test
    fun `is due when the previous app day has an outing pending its check-in`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(plannedOn(FRIDAY)))

        assertTrue(useCase(repository, SATURDAY).invoke())
    }

    @Test
    fun `is not due once the outing was checked in`() = runTest {
        val done = plannedOn(FRIDAY).copy(checkIn = CheckIn(Money(90_00), 2, LocalTime(1, 0), mood = 4))

        assertFalse(useCase(FakeOutingRepository(initialOutings = listOf(done)), SATURDAY).invoke())
    }

    @Test
    fun `is not due when the previous app day has no outing`() = runTest {
        assertFalse(useCase(FakeOutingRepository(), SATURDAY).invoke())
    }

    @Test
    fun `is not due for a pending outing from two app days back`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(plannedOn(THURSDAY)))

        assertFalse(useCase(repository, SATURDAY).invoke())
    }

    @Test
    fun `is not due when notifications are turned off`() = runTest {
        val repository = FakeOutingRepository(initialOutings = listOf(plannedOn(FRIDAY)))
        val preferences = FakeUserPreferencesRepository(isNotificationsEnabled = false)

        assertFalse(useCase(repository, SATURDAY, preferences).invoke())
    }
}

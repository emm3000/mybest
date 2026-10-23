package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.testing.FakeOutingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)
private val PLANNED = Outing(1, FRIDAY, OutingKind.PLANNED, Limits(Money(200_00), 4, LocalTime(2, 0)), checkIn = null)
private val UNPLANNED = Outing(1, FRIDAY, OutingKind.UNPLANNED, limits = null, checkIn = null)
private val COMPLETE = CheckInDraft(Money(260_00), drinks = 5, returnTime = LocalTime(1, 40), mood = 2)

class SaveCheckInUseCaseTest {

    private val repository = FakeOutingRepository(initialOutings = listOf(PLANNED))
    private val useCase = SaveCheckInUseCase(repository)

    private suspend fun savedCheckIn(): CheckIn? = repository.observeAll().first().single().checkIn

    @Test
    fun `saves a complete planned check-in and returns the outing with it`() = runTest {
        val result = useCase(PLANNED, COMPLETE)

        val expected = CheckIn(Money(260_00), 5, LocalTime(1, 40), mood = 2)
        assertEquals(SaveCheckInResult.Saved(PLANNED.copy(checkIn = expected)), result)
        assertEquals(expected, savedCheckIn())
    }

    @Test
    fun `rejects a mood outside 1 to 5`() = runTest {
        assertEquals(SaveCheckInResult.InvalidMood, useCase(PLANNED, COMPLETE.copy(mood = 0)))
        assertEquals(SaveCheckInResult.InvalidMood, useCase(PLANNED, COMPLETE.copy(mood = 6)))
        assertEquals(null, savedCheckIn())
    }

    @Test
    fun `rejects a negative spend`() = runTest {
        val result = useCase(PLANNED, COMPLETE.copy(totalSpent = Money(-1)))

        assertEquals(SaveCheckInResult.NegativeSpend, result)
        assertEquals(null, savedCheckIn())
    }

    @Test
    fun `rejects a missing spend`() = runTest {
        assertEquals(SaveCheckInResult.MissingSpend, useCase(PLANNED, COMPLETE.copy(totalSpent = null)))
    }

    @Test
    fun `requires drinks and return time for a planned outing`() = runTest {
        assertEquals(SaveCheckInResult.MissingDrinks, useCase(PLANNED, COMPLETE.copy(drinks = null)))
        assertEquals(SaveCheckInResult.MissingReturnTime, useCase(PLANNED, COMPLETE.copy(returnTime = null)))
        assertEquals(null, savedCheckIn())
    }

    @Test
    fun `ignores drinks and return time for an unplanned outing`() = runTest {
        val unplannedRepository = FakeOutingRepository(initialOutings = listOf(UNPLANNED))

        val result = SaveCheckInUseCase(unplannedRepository)(UNPLANNED, COMPLETE)

        val expected = CheckIn(Money(260_00), drinks = null, returnTime = null, mood = 2)
        assertEquals(SaveCheckInResult.Saved(UNPLANNED.copy(checkIn = expected)), result)
        assertEquals(expected, unplannedRepository.observeAll().first().single().checkIn)
    }
}

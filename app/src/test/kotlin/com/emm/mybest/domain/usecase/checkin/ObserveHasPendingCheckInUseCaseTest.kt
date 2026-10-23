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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)
private val LIMITS = Limits(Money(150_00), drinks = 4, returnTime = LocalTime(1, 30))
private val CHECK_IN = CheckIn(Money(90_00), drinks = 3, returnTime = LocalTime(1, 0), mood = 4)

class ObserveHasPendingCheckInUseCaseTest {

    private suspend fun hasPending(vararg outings: Outing): Boolean =
        ObserveHasPendingCheckInUseCase(FakeOutingRepository(initialOutings = outings.toList()))().first()

    private fun plannedOuting(checkIn: CheckIn?) = Outing(1, FRIDAY, OutingKind.PLANNED, LIMITS, checkIn)

    @Test
    fun `no outings leave nothing pending`() = runTest {
        assertFalse(hasPending())
    }

    @Test
    fun `an outing without a check-in is pending`() = runTest {
        assertTrue(hasPending(plannedOuting(checkIn = null)))
    }

    @Test
    fun `an outing with a check-in is not pending`() = runTest {
        assertFalse(hasPending(plannedOuting(CHECK_IN)))
    }
}

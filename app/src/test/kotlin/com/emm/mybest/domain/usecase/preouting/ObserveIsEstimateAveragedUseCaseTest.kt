package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.testing.FakeOutingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveIsEstimateAveragedUseCaseTest {

    private fun outing(day: Int, isCheckedIn: Boolean) = Outing(
        id = day.toLong(),
        appDay = LocalDate(2026, 9, day),
        kind = OutingKind.UNPLANNED,
        limits = null,
        checkIn = CheckIn(Money(100_00), drinks = null, returnTime = null, mood = 3).takeIf { isCheckedIn },
    )

    private fun isAveraged(vararg outings: Outing) =
        ObserveIsEstimateAveragedUseCase(FakeOutingRepository(outings.toList()))

    @Test
    fun `is false with fewer than three checked-in outings`() = runTest {
        assertFalse(isAveraged(outing(4, true), outing(11, true), outing(18, false))().first())
    }

    @Test
    fun `is true once three outings have a check-in`() = runTest {
        assertTrue(isAveraged(outing(4, true), outing(11, true), outing(18, true), outing(25, false))().first())
    }
}

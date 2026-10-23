package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.testing.FakeOutingRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)

class HasDecisionOnUseCaseTest {

    private fun outingOn(appDay: LocalDate) = Outing(1, appDay, OutingKind.UNPLANNED, limits = null, checkIn = null)

    @Test
    fun `an app day without an outing or a won night has no decision`() = runTest {
        val useCase = HasDecisionOnUseCase(FakeOutingRepository())

        assertFalse(useCase(FRIDAY))
    }

    @Test
    fun `an outing on the app day is a decision`() = runTest {
        val useCase = HasDecisionOnUseCase(FakeOutingRepository(initialOutings = listOf(outingOn(FRIDAY))))

        assertTrue(useCase(FRIDAY))
    }

    @Test
    fun `a won night on the app day is a decision`() = runTest {
        val repository = FakeOutingRepository(initialWonNights = listOf(WonNight(FRIDAY, estimatedCost = null)))

        assertTrue(HasDecisionOnUseCase(repository)(FRIDAY))
    }

    @Test
    fun `an outing on another app day is not a decision`() = runTest {
        val useCase = HasDecisionOnUseCase(FakeOutingRepository(initialOutings = listOf(outingOn(SATURDAY))))

        assertFalse(useCase(FRIDAY))
    }
}

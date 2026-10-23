package com.emm.mybest.domain.usecase.relapse

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.Relapse
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeSetupRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)
private val LIMITS = Limits(Money(150_00), drinks = 4, returnTime = LocalTime(1, 30))
private val KEPT = CheckIn(Money(100_00), drinks = 3, returnTime = LocalTime(1, 0), mood = 3)
private val OVERSPENT = KEPT.copy(totalSpent = Money(200_00))

class ObserveRelapsesUseCaseTest {

    private suspend fun relapses(outings: List<Outing>, setup: Setup = Setup()): List<Relapse> =
        ObserveRelapsesUseCase(FakeOutingRepository(outings), FakeSetupRepository(setup)).invoke().first()

    private fun planned(id: Long, day: LocalDate, checkIn: CheckIn?) =
        Outing(id, day, OutingKind.PLANNED, LIMITS, checkIn)

    private fun unplanned(id: Long, day: LocalDate) = Outing(id, day, OutingKind.UNPLANNED, null, null)

    @Test
    fun `an unplanned outing is a relapse because it was unplanned`() = runTest {
        val outing = unplanned(1, FRIDAY)

        assertEquals(listOf(Relapse(outing, setOf(RelapseReason.UNPLANNED))), relapses(listOf(outing)))
    }

    @Test
    fun `a planned outing that exceeded a limit is a relapse because it broke a limit`() = runTest {
        val outing = planned(1, FRIDAY, OVERSPENT)

        assertEquals(listOf(Relapse(outing, setOf(RelapseReason.BROKE_LIMIT))), relapses(listOf(outing)))
    }

    @Test
    fun `a planned outing that returned at 05-00 broke only the return time limit`() = runTest {
        val outing = planned(1, FRIDAY, KEPT.copy(returnTime = LocalTime(5, 0)))

        assertEquals(listOf(Relapse(outing, setOf(RelapseReason.BROKE_LIMIT))), relapses(listOf(outing)))
    }

    @Test
    fun `a planned outing that returned at 06-00 broke only the return time limit`() = runTest {
        val outing = planned(1, FRIDAY, KEPT.copy(returnTime = LocalTime(6, 0)))

        assertEquals(listOf(Relapse(outing, setOf(RelapseReason.BROKE_LIMIT))), relapses(listOf(outing)))
    }

    @Test
    fun `a planned outing pending its check-in did not break a limit`() = runTest {
        assertTrue(relapses(listOf(planned(1, FRIDAY, checkIn = null))).isEmpty())
    }

    @Test
    fun `a planned outing that kept every limit within the monthly limit is not a relapse`() = runTest {
        val outing = planned(1, FRIDAY, KEPT)

        assertTrue(relapses(listOf(outing), Setup(monthlyOutingLimit = 2)).isEmpty())
    }

    @Test
    fun `with a monthly limit of 2 only the third outing of the month is over the limit`() = runTest {
        val outings = (1..3).map { planned(it.toLong(), LocalDate(2026, 3, it * 7), KEPT) }

        val result = relapses(outings, Setup(monthlyOutingLimit = 2))

        assertEquals(listOf(Relapse(outings[2], setOf(RelapseReason.OVER_MONTHLY_LIMIT))), result)
    }

    @Test
    fun `an outing whose app day is 31 March counts in March`() = runTest {
        val outings = listOf(
            planned(1, LocalDate(2026, 3, 6), KEPT),
            planned(2, LocalDate(2026, 3, 13), KEPT),
            planned(3, LocalDate(2026, 3, 31), KEPT),
            planned(4, LocalDate(2026, 4, 1), KEPT),
        )

        val result = relapses(outings, Setup(monthlyOutingLimit = 2))

        assertEquals(listOf(outings[2]), result.map { it.outing })
    }

    @Test
    fun `without a monthly limit no outing is over the monthly limit`() = runTest {
        val outings = (1..4).map { planned(it.toLong(), LocalDate(2026, 3, it * 7), KEPT) }

        assertTrue(relapses(outings, Setup(monthlyOutingLimit = null)).isEmpty())
    }

    @Test
    fun `an outing over the monthly limit that also broke a limit has both reasons`() = runTest {
        val outings = listOf(planned(1, LocalDate(2026, 3, 6), KEPT), planned(2, LocalDate(2026, 3, 13), OVERSPENT))

        val result = relapses(outings, Setup(monthlyOutingLimit = 1))

        val reasons = setOf(RelapseReason.BROKE_LIMIT, RelapseReason.OVER_MONTHLY_LIMIT)
        assertEquals(listOf(Relapse(outings[1], reasons)), result)
    }
}

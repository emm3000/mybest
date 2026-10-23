package com.emm.mybest.domain.models

import com.emm.mybest.core.money.Money
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

private val FRIDAY = LocalDate(2026, 10, 23)
private val LIMITS = Limits(money = Money(12_000), drinks = 4, returnTime = LocalTime(2, 0))
private val KEPT_CHECK_IN = CheckIn(totalSpent = Money(10_000), drinks = 3, returnTime = LocalTime(1, 0), mood = 4)

private fun plannedOuting(checkIn: CheckIn?, limits: Limits = LIMITS): Outing =
    Outing(id = 1, appDay = FRIDAY, kind = OutingKind.PLANNED, limits = limits, checkIn = checkIn)

private fun Outing.exceededBy(kind: LimitKind): Long? = limitResults().firstOrNull { it.kind == kind }?.exceededBy

class OutingTest {

    @Test
    fun `spent S 150 against a S 120 money limit exceeds it by 3000 cents`() {
        val outing = plannedOuting(KEPT_CHECK_IN.copy(totalSpent = Money(15_000)))

        assertEquals(3_000L, outing.exceededBy(LimitKind.MONEY))
    }

    @Test
    fun `5 drinks against a limit of 4 exceeds it by 1`() {
        val outing = plannedOuting(KEPT_CHECK_IN.copy(drinks = 5))

        assertEquals(1L, outing.exceededBy(LimitKind.DRINKS))
    }

    @Test
    fun `a planned outing within every limit keeps all three`() {
        val outing = plannedOuting(KEPT_CHECK_IN)

        assertEquals(
            listOf(
                LimitResult(LimitKind.MONEY, 0),
                LimitResult(LimitKind.DRINKS, 0),
                LimitResult(LimitKind.RETURN_TIME, 0),
            ),
            outing.limitResults(),
        )
    }

    @Test
    fun `returning at 01_30 against a 02_00 limit keeps it`() {
        val outing = plannedOuting(KEPT_CHECK_IN.copy(returnTime = LocalTime(1, 30)))

        assertEquals(0L, outing.exceededBy(LimitKind.RETURN_TIME))
    }

    @Test
    fun `returning at 00_15 against a 23_30 limit exceeds it by 45 minutes across midnight`() {
        val outing = plannedOuting(
            checkIn = KEPT_CHECK_IN.copy(returnTime = LocalTime(0, 15)),
            limits = LIMITS.copy(returnTime = LocalTime(23, 30)),
        )

        assertEquals(45L, outing.exceededBy(LimitKind.RETURN_TIME))
    }

    @Test
    fun `returning at 04_30 against a 02_00 limit exceeds it by 150 minutes`() {
        val outing = plannedOuting(KEPT_CHECK_IN.copy(returnTime = LocalTime(4, 30)))

        assertEquals(150L, outing.exceededBy(LimitKind.RETURN_TIME))
    }

    @Test
    fun `unanswered drinks and return time are not evaluated`() {
        val outing = plannedOuting(KEPT_CHECK_IN.copy(drinks = null, returnTime = null))

        assertEquals(listOf(LimitResult(LimitKind.MONEY, 0)), outing.limitResults())
    }

    @Test
    fun `an unplanned outing has no limit results`() {
        val outing = Outing(
            id = 1,
            appDay = FRIDAY,
            kind = OutingKind.UNPLANNED,
            limits = null,
            checkIn = KEPT_CHECK_IN,
        )

        assertEquals(emptyList<LimitResult>(), outing.limitResults())
    }

    @Test
    fun `a planned outing pending its check-in has no limit results`() {
        assertEquals(emptyList<LimitResult>(), plannedOuting(checkIn = null).limitResults())
    }

    @Test
    fun `an outing without a check-in is pending`() {
        assertEquals(true, plannedOuting(checkIn = null).isPendingCheckIn)
    }

    @Test
    fun `an outing with a check-in is not pending`() {
        assertEquals(false, plannedOuting(KEPT_CHECK_IN).isPendingCheckIn)
    }

    @Test
    fun `an outing that exceeded a limit has broken a limit`() {
        assertEquals(true, plannedOuting(KEPT_CHECK_IN.copy(drinks = 5)).hasBrokenLimit)
    }

    @Test
    fun `an outing within every limit has not broken a limit`() {
        assertEquals(false, plannedOuting(KEPT_CHECK_IN).hasBrokenLimit)
    }

    @Test
    fun `an outing pending its check-in has not broken a limit`() {
        assertEquals(false, plannedOuting(checkIn = null).hasBrokenLimit)
    }
}

package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.LimitsKept
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WeeklySummary
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
private val PREVIOUS_SUNDAY = LocalDate(2026, 10, 18)
private val MONDAY = LocalDate(2026, 10, 19)
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val SUNDAY = LocalDate(2026, 10, 25)
private val NEXT_MONDAY = LocalDate(2026, 10, 26)
private val LIMITS = Limits(money = Money(100_00), drinks = 4, returnTime = LocalTime(2, 0))

private data class Records(
    val outings: List<Outing> = emptyList(),
)

class ObserveWeeklySummaryUseCaseTest {

    private val appDayClock = AppDayClock(FixedClock(LocalDateTime(2026, 10, 25, 20, 0).toInstant(LIMA)), LIMA)

    private fun planned(id: Long, day: LocalDate, checkIn: CheckIn? = null) =
        Outing(id, day, OutingKind.PLANNED, LIMITS, checkIn)

    private fun unplanned(id: Long, day: LocalDate, checkIn: CheckIn? = null) =
        Outing(id, day, OutingKind.UNPLANNED, null, checkIn)

    private fun checkIn(spent: Long, drinks: Int = 2, returnTime: LocalTime = LocalTime(1, 0)) =
        CheckIn(totalSpent = Money(spent), drinks = drinks, returnTime = returnTime, mood = 3)

    private fun useCaseOf(records: Records) = ObserveWeeklySummaryUseCase(
        WeeklySummarySections(outings = ObserveWeekOutingsUseCase(FakeOutingRepository(records.outings))),
    )

    private suspend fun summaryOf(records: Records, appDay: LocalDate = SUNDAY): WeeklySummary =
        useCaseOf(records)(appDay).first()

    @Test
    fun `counts the outings of the week`() = runTest {
        val outings = listOf(unplanned(1, PREVIOUS_SUNDAY), planned(2, FRIDAY), unplanned(3, SATURDAY))

        assertEquals(2, summaryOf(Records(outings)).outings.count)
    }

    @Test
    fun `sums the check-in totals as the week spend`() = runTest {
        val outings = listOf(planned(1, FRIDAY, checkIn(80_00)), planned(2, SATURDAY, checkIn(45_50)))

        assertEquals(Money(125_50), summaryOf(Records(outings)).outings.totalSpent)
    }

    @Test
    fun `counts the outings pending their check-in`() = runTest {
        val outings = listOf(planned(1, FRIDAY, checkIn(80_00)), planned(2, SATURDAY), unplanned(3, SUNDAY))

        assertEquals(2, summaryOf(Records(outings)).outings.pendingCheckIns)
    }

    @Test
    fun `counts the planned outings that kept every limit`() = runTest {
        val outings = listOf(
            planned(1, MONDAY, checkIn(90_00)),
            planned(2, FRIDAY, checkIn(120_00)),
            planned(3, SATURDAY),
            unplanned(4, SUNDAY, checkIn(30_00)),
        )

        assertEquals(LimitsKept(kept = 1, checkedIn = 2), summaryOf(Records(outings)).outings.limitsKept)
    }

    @Test
    fun `counts a return between 05 00 and 11 59 as a broken limit`() = runTest {
        val outings = listOf(planned(1, FRIDAY, checkIn(50_00, returnTime = LocalTime(9, 0))))

        assertEquals(LimitsKept(kept = 0, checkedIn = 1), summaryOf(Records(outings)).outings.limitsKept)
    }

    @Test
    fun `an outing at Sunday 02 00 belongs to the Saturday app day of the week`() = runTest {
        val appDay = appDayClock.appDayOf(LocalDateTime(2026, 10, 25, 2, 0).toInstant(LIMA))
        val outings = listOf(unplanned(1, appDay), unplanned(2, NEXT_MONDAY))

        assertEquals(SATURDAY, appDay)
        assertEquals(1, summaryOf(Records(outings), appDay = SUNDAY).outings.count)
    }

    @Test
    fun `Monday 03 00 asks for the previous week because its app day is Sunday`() = runTest {
        val appDay = appDayClock.appDayOf(LocalDateTime(2026, 10, 26, 3, 0).toInstant(LIMA))
        val outings = listOf(unplanned(1, FRIDAY), unplanned(2, NEXT_MONDAY))

        assertEquals(SUNDAY, appDay)
        assertEquals(1, summaryOf(Records(outings), appDay).outings.count)
    }
}

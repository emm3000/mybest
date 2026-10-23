package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.LimitsKept
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.ReturnTime
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeeklySummary
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.domain.usecase.dailycheck.ObserveWeekProgressUseCase
import com.emm.mybest.domain.usecase.dailycheck.WeekProgress
import com.emm.mybest.domain.usecase.relapse.ObserveRelapsesUseCase
import com.emm.mybest.domain.usecase.relapse.ObserveReturnTimesUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeighInTrendUseCase
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val PREVIOUS_FRIDAY = LocalDate(2026, 10, 16)
private val PREVIOUS_SATURDAY = LocalDate(2026, 10, 17)
private val PREVIOUS_SUNDAY = LocalDate(2026, 10, 18)
private val MONDAY = LocalDate(2026, 10, 19)
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val SUNDAY = LocalDate(2026, 10, 25)
private val NEXT_MONDAY = LocalDate(2026, 10, 26)
private val LIMITS = Limits(money = Money(100_00), drinks = 4, returnTime = LocalTime(2, 0))

private data class Records(
    val outings: List<Outing> = emptyList(),
    val checks: List<DailyCheck> = emptyList(),
    val setup: Setup = Setup(),
    val weights: List<Float> = emptyList(),
)

class ObserveWeeklySummaryUseCaseTest {

    private val appDayClock = AppDayClock(FixedClock(LocalDateTime(2026, 10, 25, 20, 0).toInstant(LIMA)), LIMA)

    private fun planned(id: Long, day: LocalDate, checkIn: CheckIn? = null) =
        Outing(id, day, OutingKind.PLANNED, LIMITS, checkIn)

    private fun unplanned(id: Long, day: LocalDate, checkIn: CheckIn? = null) =
        Outing(id, day, OutingKind.UNPLANNED, null, checkIn)

    private fun checkIn(spent: Long, drinks: Int = 2, returnTime: LocalTime = LocalTime(1, 0)) =
        CheckIn(totalSpent = Money(spent), drinks = drinks, returnTime = returnTime, mood = 3)

    private fun useCaseOf(records: Records): ObserveWeeklySummaryUseCase {
        val outingRepository = FakeOutingRepository(records.outings)
        val setupRepository = FakeSetupRepository(records.setup)
        val checkRepository = FakeDailyCheckRepository(records.checks)
        val relapses = ObserveRelapsesUseCase(outingRepository, setupRepository)
        return ObserveWeeklySummaryUseCase(
            WeeklySummarySections(
                outings = ObserveWeekOutingsUseCase(outingRepository),
                recovery = ObserveWeekRecoveryUseCase(
                    relapses,
                    ObserveReturnTimesUseCase(relapses, checkRepository, appDayClock),
                ),
                weekProgress = ObserveWeekProgressUseCase(checkRepository, setupRepository),
                weighInTrend = ObserveWeighInTrendUseCase(weightRepositoryOf(records.weights), setupRepository),
            ),
        )
    }

    private fun weightRepositoryOf(weights: List<Float>): WeightRepository {
        val entries = weights.mapIndexed { index, kg ->
            WeightEntry(id = "w$index", date = LocalDate(2026, 10, 1 + index), weight = kg)
        }
        val repository = mockk<WeightRepository>()
        every { repository.getWeightProgress() } returns flowOf(entries)
        return repository
    }

    private fun dietDone(day: LocalDate) = DailyCheck(day, DailyCheckType.DIET, isDone = true)

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

    @Test
    fun `lists the relapses of the week`() = runTest {
        val outings = listOf(
            unplanned(1, PREVIOUS_SUNDAY),
            planned(2, MONDAY, checkIn(90_00)),
            unplanned(3, FRIDAY),
            planned(4, SATURDAY, checkIn(120_00)),
        )

        assertEquals(listOf(3L, 4L), summaryOf(Records(outings)).recovery.relapses.map { it.outing.id })
    }

    @Test
    fun `gives the latest return time`() = runTest {
        val records = Records(listOf(unplanned(1, FRIDAY)), listOf(dietDone(SATURDAY)))

        assertEquals(ReturnTime(FRIDAY, SATURDAY, days = 1), summaryOf(records).recovery.latestReturnTime)
    }

    @Test
    fun `a past week gives the latest return time of its own relapses`() = runTest {
        val records = Records(
            outings = listOf(unplanned(1, PREVIOUS_FRIDAY), unplanned(2, FRIDAY)),
            checks = listOf(dietDone(PREVIOUS_SATURDAY), dietDone(SATURDAY)),
        )

        val latest = summaryOf(records, appDay = PREVIOUS_SUNDAY).recovery.latestReturnTime

        assertEquals(ReturnTime(PREVIOUS_FRIDAY, PREVIOUS_SATURDAY, days = 1), latest)
    }

    @Test
    fun `has no latest return time when the week has no relapse`() = runTest {
        val records = Records(listOf(unplanned(1, PREVIOUS_FRIDAY)), listOf(dietDone(PREVIOUS_SATURDAY)))

        assertNull(summaryOf(records).recovery.latestReturnTime)
    }

    @Test
    fun `gives the return time history of every relapse newest first`() = runTest {
        val records = Records(
            outings = listOf(unplanned(1, PREVIOUS_SUNDAY), unplanned(2, FRIDAY)),
            checks = listOf(dietDone(MONDAY), dietDone(SATURDAY)),
        )

        val history = listOf(ReturnTime(FRIDAY, SATURDAY, days = 1), ReturnTime(PREVIOUS_SUNDAY, MONDAY, days = 1))
        assertEquals(history, summaryOf(records).recovery.returnTimeHistory)
    }

    @Test
    fun `counts the checks done this week against each weekly target`() = runTest {
        val gymDays = listOf(PREVIOUS_SUNDAY, MONDAY, FRIDAY, NEXT_MONDAY)
        val records = Records(
            checks = gymDays.map { DailyCheck(it, DailyCheckType.GYM, isDone = true) },
            setup = Setup(weeklyTargets = mapOf(DailyCheckType.GYM to 3, DailyCheckType.DIET to 5)),
        )

        val expected = mapOf(DailyCheckType.GYM to WeekProgress(2, 3), DailyCheckType.DIET to WeekProgress(0, 5))
        assertEquals(expected, summaryOf(records).checks)
    }

    @Test
    fun `gives the weigh-in trend`() = runTest {
        val records = Records(weights = listOf(82f, 80.5f), setup = Setup(targetWeightKg = 78f))

        val expected = WeighInTrend(latestKg = 80.5f, changeSincePreviousKg = -1.5f, remainingToTargetKg = 2.5f)
        assertEquals(expected, summaryOf(records).weighInTrend)
    }
}

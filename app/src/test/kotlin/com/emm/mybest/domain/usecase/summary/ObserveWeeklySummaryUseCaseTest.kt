package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.GoalProgress
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.LimitsKept
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.models.ReturnTime
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeekProgress
import com.emm.mybest.domain.models.WeeklySummary
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.WeightRepository
import com.emm.mybest.domain.usecase.dailycheck.ObserveWeekProgressUseCase
import com.emm.mybest.domain.usecase.debts.ObserveDebtSummaryUseCase
import com.emm.mybest.domain.usecase.goal.ObserveGoalProgressUseCase
import com.emm.mybest.domain.usecase.relapse.ObserveRelapsesUseCase
import com.emm.mybest.domain.usecase.relapse.ObserveReturnTimesUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeighInTrendUseCase
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
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
private val SATURDAY_BEFORE_PREVIOUS = LocalDate(2026, 10, 10)
private val PREVIOUS_FRIDAY = LocalDate(2026, 10, 16)
private val PREVIOUS_SATURDAY = LocalDate(2026, 10, 17)
private val PREVIOUS_SUNDAY = LocalDate(2026, 10, 18)
private val MONDAY = LocalDate(2026, 10, 19)
private val FRIDAY = LocalDate(2026, 10, 23)
private val SATURDAY = LocalDate(2026, 10, 24)
private val SUNDAY = LocalDate(2026, 10, 25)
private val NEXT_MONDAY = LocalDate(2026, 10, 26)
private val OCTOBER = YearMonthValue(2026, 10)
private val SEPTEMBER = YearMonthValue(2026, 9)
private val LIMITS = Limits(money = Money(100_00), drinks = 4, returnTime = LocalTime(2, 0))

private data class Records(
    val outings: List<Outing> = emptyList(),
    val checks: List<DailyCheck> = emptyList(),
    val setup: Setup = Setup(),
    val weights: List<Float> = emptyList(),
    val weighIns: List<WeightEntry> = emptyList(),
    val wonNights: List<WonNight> = emptyList(),
    val debts: List<Debt> = emptyList(),
    val payments: List<DebtPayment> = emptyList(),
    val goal: PurchaseGoal? = null,
    val contributions: List<GoalContribution> = emptyList(),
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
        val outingRepository = FakeOutingRepository(records.outings, records.wonNights)
        val setupRepository = FakeSetupRepository(records.setup)
        val checkRepository = FakeDailyCheckRepository(records.checks)
        val relapses = ObserveRelapsesUseCase(outingRepository, setupRepository)
        return ObserveWeeklySummaryUseCase(
            WeeklySummarySections(
                outings = ObserveWeekOutingsUseCase(outingRepository),
                relapses = ObserveWeekRelapsesUseCase(
                    relapses,
                    ObserveReturnTimesUseCase(relapses, checkRepository, appDayClock),
                ),
                weekProgress = ObserveWeekProgressUseCase(checkRepository, setupRepository),
                weighInTrend = ObserveWeighInTrendUseCase(weightRepositoryOf(records), setupRepository),
                money = moneyOf(records, outingRepository),
            ),
        )
    }

    private fun moneyOf(records: Records, outingRepository: FakeOutingRepository): ObserveWeekMoneyUseCase {
        val debtRepository = FakeDebtRepository(records.debts, records.payments)
        val goalRepository = FakePurchaseGoalRepository(records.goal, records.contributions)
        return ObserveWeekMoneyUseCase(
            outingRepository,
            ObserveWeekDebtUseCase(ObserveDebtSummaryUseCase(debtRepository), debtRepository),
            ObserveGoalProgressUseCase(goalRepository),
        )
    }

    private fun weightRepositoryOf(records: Records): WeightRepository {
        val entries = records.weights.mapIndexed { index, kg ->
            WeightEntry(id = "w$index", date = LocalDate(2026, 10, 1 + index), weight = kg)
        } + records.weighIns
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

        assertEquals(listOf(3L, 4L), summaryOf(Records(outings)).relapses.relapses.map { it.outing.id })
    }

    @Test
    fun `gives the latest return time`() = runTest {
        val records = Records(listOf(unplanned(1, FRIDAY)), listOf(dietDone(SATURDAY)))

        assertEquals(ReturnTime(FRIDAY, SATURDAY, days = 1), summaryOf(records).relapses.latestReturnTime)
    }

    @Test
    fun `a past week gives the latest return time of its own relapses`() = runTest {
        val records = Records(
            outings = listOf(unplanned(1, PREVIOUS_FRIDAY), unplanned(2, FRIDAY)),
            checks = listOf(dietDone(PREVIOUS_SATURDAY), dietDone(SATURDAY)),
        )

        val latest = summaryOf(records, appDay = PREVIOUS_SUNDAY).relapses.latestReturnTime

        assertEquals(ReturnTime(PREVIOUS_FRIDAY, PREVIOUS_SATURDAY, days = 1), latest)
    }

    @Test
    fun `has no latest return time when the week has no relapse`() = runTest {
        val records = Records(listOf(unplanned(1, PREVIOUS_FRIDAY)), listOf(dietDone(PREVIOUS_SATURDAY)))

        assertNull(summaryOf(records).relapses.latestReturnTime)
    }

    @Test
    fun `gives the return time history of every relapse newest first`() = runTest {
        val records = Records(
            outings = listOf(unplanned(1, PREVIOUS_SUNDAY), unplanned(2, FRIDAY)),
            checks = listOf(dietDone(MONDAY), dietDone(SATURDAY)),
        )

        val history = listOf(ReturnTime(FRIDAY, SATURDAY, days = 1), ReturnTime(PREVIOUS_SUNDAY, MONDAY, days = 1))
        assertEquals(history, summaryOf(records).relapses.returnTimeHistory)
    }

    @Test
    fun `a past week leaves out the relapses that came after it from the history`() = runTest {
        val records = Records(
            outings = listOf(unplanned(1, PREVIOUS_FRIDAY), unplanned(2, FRIDAY)),
            checks = listOf(dietDone(PREVIOUS_SATURDAY), dietDone(SATURDAY)),
        )

        val history = summaryOf(records, appDay = PREVIOUS_SUNDAY).relapses.returnTimeHistory

        assertEquals(listOf(ReturnTime(PREVIOUS_FRIDAY, PREVIOUS_SATURDAY, days = 1)), history)
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

    private fun weighIn(day: LocalDate, kg: Float) = WeightEntry(id = "w$day", date = day, weight = kg)

    @Test
    fun `a past week gives the weigh-in trend as of its Sunday`() = runTest {
        val weighIns = listOf(
            weighIn(SATURDAY_BEFORE_PREVIOUS, 82f),
            weighIn(PREVIOUS_SATURDAY, 80.5f),
            weighIn(SATURDAY, 79f),
        )
        val records = Records(weighIns = weighIns, setup = Setup(targetWeightKg = 78f))

        assertEquals(WeighInTrend(80.5f, -1.5f, 2.5f), summaryOf(records, PREVIOUS_SUNDAY).weighInTrend)
        assertEquals(WeighInTrend(79f, -1.5f, 1f), summaryOf(records, SUNDAY).weighInTrend)
    }

    @Test
    fun `a past week counts the weigh-in dated on its Sunday and not the one on the next Monday`() = runTest {
        val records = Records(weighIns = listOf(weighIn(PREVIOUS_SUNDAY, 80f), weighIn(MONDAY, 79f)))

        assertEquals(WeighInTrend(latestKg = 80f), summaryOf(records, PREVIOUS_SUNDAY).weighInTrend)
    }

    @Test
    fun `a past week before the first weigh-in gives an empty trend`() = runTest {
        val records = Records(weighIns = listOf(weighIn(SATURDAY, 79f)))

        assertEquals(WeighInTrend(), summaryOf(records, PREVIOUS_SUNDAY).weighInTrend)
    }

    private fun debt(id: Long, balance: Long) =
        Debt(id, "Debt $id", balance = Money(balance), installment = Money(100_00), paymentDay = 5)

    @Test
    fun `lists the won nights of the week with their estimated total`() = runTest {
        val wonNights = listOf(
            WonNight(LocalDate(2026, 10, 16), Money(300_00)),
            WonNight(FRIDAY, Money(380_00)),
            WonNight(SATURDAY, estimatedCost = null),
        )

        val money = summaryOf(Records(wonNights = wonNights)).money

        assertEquals(wonNights.drop(1), money.wonNights)
        assertEquals(Money(380_00), money.notSpentEstimated)
    }

    @Test
    fun `sums the total debt balance`() = runTest {
        val records = Records(debts = listOf(debt(1, 500_00), debt(2, 300_00)))

        assertEquals(Money(800_00), summaryOf(records).money.debt.totalBalance)
    }

    @Test
    fun `counts the installments paid this month against the number of debts`() = runTest {
        val records = Records(
            debts = listOf(debt(1, 500_00), debt(2, 300_00)),
            payments = listOf(
                DebtPayment(1, OCTOBER, Money(100_00)),
                DebtPayment(1, SEPTEMBER, Money(100_00)),
                DebtPayment(2, SEPTEMBER, Money(100_00)),
            ),
        )

        val debt = summaryOf(records).money.debt

        assertEquals(1, debt.installmentsPaidThisMonth)
        assertEquals(2, debt.debtCount)
    }

    @Test
    fun `gives the goal contributed against its price`() = runTest {
        val contributions =
            listOf(GoalContribution(1, MONDAY, Money(200_00)), GoalContribution(2, FRIDAY, Money(300_00)))
        val records = Records(goal = PurchaseGoal("Laptop", Money(3000_00)), contributions = contributions)

        val goal = summaryOf(records).money.goal

        assertEquals(Money(500_00), goal?.contributed)
        assertEquals(Money(3000_00), goal?.goal?.price)
    }

    @Test
    fun `a past week counts only the contributions made by its Sunday`() = runTest {
        val contributions =
            listOf(GoalContribution(1, PREVIOUS_FRIDAY, Money(100_00)), GoalContribution(2, FRIDAY, Money(50_00)))
        val records = Records(goal = PurchaseGoal("Laptop", Money(1000_00)), contributions = contributions)

        val past = summaryOf(records, PREVIOUS_SUNDAY).money.goal
        val current = summaryOf(records, SUNDAY).money.goal

        assertEquals(listOf(Money(100_00), Money(900_00), 10, 1), past?.let(::figuresOf))
        assertEquals(listOf(Money(150_00), Money(850_00), 15, 2), current?.let(::figuresOf))
    }

    @Test
    fun `a past week counts the contribution dated on its Sunday and not the one on the next Monday`() = runTest {
        val contributions =
            listOf(GoalContribution(1, PREVIOUS_SUNDAY, Money(100_00)), GoalContribution(2, MONDAY, Money(50_00)))
        val records = Records(goal = PurchaseGoal("Laptop", Money(1000_00)), contributions = contributions)

        val goal = summaryOf(records, PREVIOUS_SUNDAY).money.goal

        assertEquals(listOf(Money(100_00), Money(900_00), 10, 1), goal?.let(::figuresOf))
    }

    private fun figuresOf(goal: GoalProgress) =
        listOf(goal.contributed, goal.left, goal.percent, goal.contributions.size)

    @Test
    fun `has no goal progress without a purchase goal`() = runTest {
        assertNull(summaryOf(Records()).money.goal)
    }
}

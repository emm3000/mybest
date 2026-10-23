package com.emm.mybest.domain.usecase.goal

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.usecase.preouting.ObserveNotSpentThisMonthUseCase
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val GOAL = PurchaseGoal("Zapatillas", Money(450_00))
private val OCTOBER_9 = LocalDate(2026, 10, 9)
private val OCTOBER_16 = LocalDate(2026, 10, 16)
private val LIMA = TimeZone.of("America/Lima")
private val CLOCK = AppDayClock(FixedClock(LocalDateTime(2026, 10, 23, 12, 0).toInstant(LIMA)), LIMA)

class ObserveGoalProgressUseCaseTest {

    private fun useCase(goal: PurchaseGoal?, contributions: List<GoalContribution>) =
        ObserveGoalProgressUseCase(FakePurchaseGoalRepository(goal, contributions))

    @Test
    fun `is null without a goal`() = runTest {
        assertNull(useCase(goal = null, contributions = emptyList())().first())
    }

    @Test
    fun `progress equals the sum of contributions`() = runTest {
        val contributions = listOf(
            GoalContribution(1, OCTOBER_9, Money(100_00)),
            GoalContribution(2, OCTOBER_16, Money(80_00)),
        )

        val progress = useCase(GOAL, contributions)().first()!!

        assertEquals(Money(180_00), progress.contributed)
        assertEquals(Money(270_00), progress.left)
        assertEquals(40, progress.percent)
    }

    @Test
    fun `won nights do not change the progress`() = runTest {
        val wonNights = listOf(WonNight(OCTOBER_9, Money(380_00)))
        val notSpent = ObserveNotSpentThisMonthUseCase(FakeOutingRepository(initialWonNights = wonNights), CLOCK)
        val contributions = listOf(GoalContribution(1, OCTOBER_9, Money(100_00)))

        val progress = useCase(GOAL, contributions)().first()!!

        assertEquals(Money(380_00), notSpent().first())
        assertEquals(Money(100_00), progress.contributed)
        assertEquals(Money(350_00), progress.left)
    }

    @Test
    fun `left is floored at zero and percent at one hundred`() = runTest {
        val contributions = listOf(GoalContribution(1, OCTOBER_9, Money(500_00)))

        val progress = useCase(GOAL, contributions)().first()!!

        assertEquals(Money.ZERO, progress.left)
        assertEquals(100, progress.percent)
    }

    @Test
    fun `is zero progress without contributions`() = runTest {
        val progress = useCase(GOAL, emptyList())().first()!!

        assertEquals(Money.ZERO, progress.contributed)
        assertEquals(GOAL.price, progress.left)
        assertEquals(0, progress.percent)
    }

    @Test
    fun `lists contributions newest first`() = runTest {
        val older = GoalContribution(1, OCTOBER_9, Money(10_00))
        val newer = GoalContribution(2, OCTOBER_16, Money(20_00))
        val sameDayLater = GoalContribution(3, OCTOBER_16, Money(30_00))

        val progress = useCase(GOAL, listOf(older, newer, sameDayLater))().first()!!

        assertEquals(listOf(sameDayLater, newer, older), progress.contributions)
    }
}

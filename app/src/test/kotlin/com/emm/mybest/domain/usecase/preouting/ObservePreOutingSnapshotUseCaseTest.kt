package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.DebtPayment
import com.emm.mybest.domain.models.GoalContribution
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PreOutingSnapshot
import com.emm.mybest.domain.models.PurchaseGoal
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FakePurchaseGoalRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private val LIMA = TimeZone.of("America/Lima")
private val TYPICAL_COST = Money(380_00)

class ObservePreOutingSnapshotUseCaseTest {

    private class Rig(
        today: LocalDate = LocalDate(2026, 10, 23),
        setup: Setup = Setup(),
        outings: List<Outing> = emptyList(),
        debts: List<Debt> = emptyList(),
        payments: List<DebtPayment> = emptyList(),
        goal: PurchaseGoal? = null,
        contributions: List<GoalContribution> = emptyList(),
    ) {
        val outingRepository = FakeOutingRepository(outings)
        private val clock =
            AppDayClock(FixedClock(LocalDateTime(today, kotlinx.datetime.LocalTime(12, 0)).toInstant(LIMA)), LIMA)
        private val setupRepository = FakeSetupRepository(setup)
        private val estimate = GetEstimatedOutingCostUseCase(setupRepository, outingRepository)
        val useCase = ObservePreOutingSnapshotUseCase(
            PreOutingUseCases(
                outingMonth = ObserveOutingMonthUseCase(outingRepository, setupRepository, clock),
                nearestInstallment = ObserveNearestInstallmentUseCase(
                    FakeDebtRepository(debts, payments),
                    estimate,
                    clock,
                ),
                purchaseGoalLeft = ObservePurchaseGoalLeftUseCase(FakePurchaseGoalRepository(goal, contributions)),
                estimatedOutingCost = estimate,
                isEstimateAveraged = ObserveIsEstimateAveragedUseCase(outingRepository),
                notSpentThisMonth = ObserveNotSpentThisMonthUseCase(outingRepository, clock),
            ),
        )

        suspend fun snapshot(): PreOutingSnapshot = useCase().first()
    }

    private fun debt(id: Long, name: String, installment: Long, day: Int) =
        Debt(id, name, balance = Money(5_000_00), installment = Money(installment), paymentDay = day)

    private fun checkedIn(id: Long, day: LocalDate, spent: Long) = Outing(
        id,
        day,
        OutingKind.UNPLANNED,
        limits = null,
        checkIn = CheckIn(Money(spent), drinks = null, returnTime = null, mood = 3),
    )

    private fun pending(id: Long, day: LocalDate) = Outing(id, day, OutingKind.UNPLANNED, null, null)

    @Test
    fun `picks the earliest payment date on or after today`() = runTest {
        val debts = listOf(debt(1, "Banco", 400_00, 28), debt(2, "Tarjeta", 500_00, 23), debt(3, "Auto", 300_00, 25))

        val share = Rig(debts = debts).snapshot().nearestInstallment

        assertEquals("Tarjeta", share?.debtName)
        assertEquals(LocalDate(2026, 10, 23), share?.paymentDate)
    }

    @Test
    fun `a debt paid this month falls due next month`() = runTest {
        val debts = listOf(debt(1, "Banco", 400_00, 28), debt(2, "Tarjeta", 500_00, 30))
        val paid = listOf(DebtPayment(1, YearMonthValue(2026, 10), Money(400_00)))

        val share = Rig(debts = debts, payments = paid).snapshot().nearestInstallment

        assertEquals("Tarjeta", share?.debtName)
    }

    @Test
    fun `the only debt paid this month uses next month's date`() = runTest {
        val paid = listOf(DebtPayment(1, YearMonthValue(2026, 10), Money(400_00)))

        val share = Rig(debts = listOf(debt(1, "Banco", 400_00, 28)), payments = paid).snapshot().nearestInstallment

        assertEquals(LocalDate(2026, 11, 28), share?.paymentDate)
    }

    @Test
    fun `an unpaid debt whose date passed falls due next month`() = runTest {
        val share = Rig(debts = listOf(debt(1, "Banco", 400_00, 10))).snapshot().nearestInstallment

        assertEquals(LocalDate(2026, 11, 10), share?.paymentDate)
    }

    @Test
    fun `payment day 31 in a 30-day month falls on the 30th`() = runTest {
        val rig = Rig(today = LocalDate(2026, 11, 3), debts = listOf(debt(1, "Banco", 400_00, 31)))

        assertEquals(LocalDate(2026, 11, 30), rig.snapshot().nearestInstallment?.paymentDate)
    }

    @Test
    fun `night percent is the estimate over the installment`() = runTest {
        val rig = Rig(setup = Setup(typicalOutingCost = TYPICAL_COST), debts = listOf(debt(1, "Banco", 1_652_00, 28)))

        assertEquals(23, rig.snapshot().nearestInstallment?.nightPercent)
    }

    @Test
    fun `night percent is null without an estimate`() = runTest {
        val share = Rig(debts = listOf(debt(1, "Banco", 400_00, 28))).snapshot().nearestInstallment

        assertEquals(InstallmentShare("Banco", LocalDate(2026, 10, 28), Money(400_00), nightPercent = null), share)
    }

    @Test
    fun `there is no nearest installment without debts`() = runTest {
        assertNull(Rig().snapshot().nearestInstallment)
    }

    @Test
    fun `purchase goal left is the price minus contributions`() = runTest {
        val contributions = listOf(GoalContribution(1, LocalDate(2026, 10, 2), Money(100_00)))

        val left = Rig(goal = PurchaseGoal("Zapatillas", Money(370_00)), contributions = contributions)
            .snapshot().purchaseGoalLeft

        assertEquals(Money(270_00), left)
    }

    @Test
    fun `purchase goal left never goes below zero`() = runTest {
        val contributions = listOf(GoalContribution(1, LocalDate(2026, 10, 2), Money(500_00)))

        val left = Rig(goal = PurchaseGoal("Zapatillas", Money(370_00)), contributions = contributions)
            .snapshot().purchaseGoalLeft

        assertEquals(Money.ZERO, left)
    }

    @Test
    fun `purchase goal left is null without a goal`() = runTest {
        assertNull(Rig().snapshot().purchaseGoalLeft)
    }

    @Test
    fun `the next outing is over the limit when outings plus one pass it`() = runTest {
        val outings = listOf(checkedIn(1, LocalDate(2026, 10, 2), 100_00), pending(2, LocalDate(2026, 10, 9)))

        val snapshot = Rig(setup = Setup(monthlyOutingLimit = 2), outings = outings).snapshot()

        assertTrue(snapshot.isNextOutingOverLimit)
        assertEquals(2, snapshot.outingsThisMonth)
    }

    @Test
    fun `the next outing is within the limit while one slot remains`() = runTest {
        val outings = listOf(checkedIn(1, LocalDate(2026, 10, 2), 100_00))

        assertFalse(Rig(setup = Setup(monthlyOutingLimit = 2), outings = outings).snapshot().isNextOutingOverLimit)
    }

    @Test
    fun `the next outing is never over an unset limit`() = runTest {
        val outings = listOf(checkedIn(1, LocalDate(2026, 10, 2), 100_00))

        assertFalse(Rig(outings = outings).snapshot().isNextOutingOverLimit)
    }

    @Test
    fun `spent on outings counts only this month's check-ins`() = runTest {
        val outings = listOf(
            checkedIn(1, LocalDate(2026, 9, 26), 900_00),
            checkedIn(2, LocalDate(2026, 10, 2), 100_00),
            checkedIn(3, LocalDate(2026, 10, 9), 250_00),
            pending(4, LocalDate(2026, 10, 16)),
        )

        assertEquals(Money(350_00), Rig(outings = outings).snapshot().spentOnOutingsThisMonth)
    }

    @Test
    fun `the estimate is not averaged until three outings have a check-in`() = runTest {
        val setup = Setup(typicalOutingCost = TYPICAL_COST)
        val two = listOf(checkedIn(1, LocalDate(2026, 10, 2), 100_00), checkedIn(2, LocalDate(2026, 10, 9), 100_00))
        val three = two + checkedIn(3, LocalDate(2026, 10, 16), 100_00)

        assertFalse(Rig(setup = setup, outings = two).snapshot().isEstimateAveraged)
        assertTrue(Rig(setup = setup, outings = three).snapshot().isEstimateAveraged)
    }

    @Test
    fun `recording a won night changes only the not spent counter`() = runTest {
        val rig = Rig(
            setup = Setup(typicalOutingCost = TYPICAL_COST, monthlyOutingLimit = 3),
            debts = listOf(debt(1, "Banco", 1_000_00, 28)),
            goal = PurchaseGoal("Zapatillas", Money(370_00)),
        )
        val before = rig.snapshot()

        rig.outingRepository.recordWonNight(WonNight(LocalDate(2026, 10, 23), TYPICAL_COST))

        assertEquals(before.copy(notSpentThisMonth = TYPICAL_COST), rig.snapshot())
    }
}

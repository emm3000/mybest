package com.emm.mybest.features.home.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Debt
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeekProgress
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.home.HomeDestination
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeDebtRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import com.emm.mybest.testing.MutableClock
import com.emm.mybest.testing.VirtualTimeClock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

private val FIXED_DATE = LocalDate(2026, 5, 17)
private val FIXED_CLOCK = object : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(1_779_019_200L)
}
private val SATURDAY_AT_TWO = Instant.parse("2026-10-24T02:00:00Z")
private val FRIDAY = LocalDate(2026, 10, 23)
private val FRIDAY_BEFORE_EVENING = Instant.parse("2026-10-23T17:59:00Z")
private val FRIDAY_EVENING_ENDS = Instant.parse("2026-10-23T18:00:30Z")
private val FRIDAY_EVENING = Instant.parse("2026-10-23T20:00:00Z")

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(
        clock: Clock = FIXED_CLOCK,
        fixture: HomeUseCasesFixture = HomeUseCasesFixture(),
    ): HomeViewModel {
        val appDayClock = AppDayClock(clock, TimeZone.UTC)
        return HomeViewModel(fixture.build(appDayClock), appDayClock)
    }

    @Test
    fun `loaded state carries today`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(FIXED_DATE, state.today)
    }

    @Test
    fun `empty weights produces null lastWeightKg and null previousWeightKg`() = runTest {
        val viewModel = buildViewModel(fixture = HomeUseCasesFixture(weights = emptyList()))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.lastWeightKg)
        assertNull(state.previousWeightKg)
    }

    @Test
    fun `two weights produces both lastWeightKg and previousWeightKg`() = runTest {
        val weights = listOf(
            WeightEntry(id = "1", date = FIXED_DATE.minus(DatePeriod(days = 1)), weight = 80.0f),
            WeightEntry(id = "2", date = FIXED_DATE, weight = 79.5f),
        )
        val viewModel = buildViewModel(fixture = HomeUseCasesFixture(weights = weights))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(79.5f, state.lastWeightKg)
        assertEquals(80.0f, state.previousWeightKg)
    }

    @Test
    fun `photos with type TRUNK and last 4 days ago produces correct state`() = runTest {
        val photoDate = FIXED_DATE.minus(DatePeriod(days = 4))
        val photos = listOf(
            ProgressPhoto(
                id = "p1",
                date = photoDate,
                type = PhotoType.TRUNK,
                photoPath = "/path/photo.jpg",
                createdAt = 0L,
            ),
        )
        val viewModel = buildViewModel(fixture = HomeUseCasesFixture(photos = photos))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PhotoType.TRUNK, state.lastPhotoType)
        assertEquals(4, state.lastPhotoDaysAgo)
    }

    @Test
    fun `state lists the four daily checks in order with their done flag`() = runTest {
        val dailyChecks = FakeDailyCheckRepository(listOf(DailyCheck(FIXED_DATE, DailyCheckType.DIET, true)))
        val viewModel = buildViewModel(fixture = HomeUseCasesFixture(dailyChecks = dailyChecks))
        advanceUntilIdle()

        val checks = viewModel.state.value.checks
        assertEquals(
            listOf(DailyCheckType.GYM, DailyCheckType.DIET, DailyCheckType.ENGLISH, DailyCheckType.ALCOHOL),
            checks.map { it.type },
        )
        assertEquals(listOf(false, true, false, false), checks.map { it.isDone })
    }

    @Test
    fun `a check without a weekly target has no progress`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.checks.all { it.progress == null })
    }

    @Test
    fun `a check with a weekly target carries the week count`() = runTest {
        val dailyChecks = FakeDailyCheckRepository(listOf(DailyCheck(FIXED_DATE, DailyCheckType.GYM, true)))
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.GYM to 4))
        val viewModel = buildViewModel(fixture = HomeUseCasesFixture(dailyChecks = dailyChecks, setup = setup))
        advanceUntilIdle()

        val gym = viewModel.state.value.checks.first { it.type == DailyCheckType.GYM }
        assertEquals(WeekProgress(count = 1, target = 4), gym.progress)
    }

    @Test
    fun `toggling a check persists it for today`() = runTest {
        val dailyChecks = FakeDailyCheckRepository()
        val viewModel = buildViewModel(fixture = HomeUseCasesFixture(dailyChecks = dailyChecks))
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.ToggleDailyCheck(DailyCheckType.GYM))
        advanceUntilIdle()

        assertEquals(listOf(DailyCheck(FIXED_DATE, DailyCheckType.GYM, true)), dailyChecks.writes)
        assertTrue(viewModel.state.value.checks.first { it.type == DailyCheckType.GYM }.isDone)
    }

    @Test
    fun `a toggle at Saturday 02 00 writes Friday`() = runTest {
        val dailyChecks = FakeDailyCheckRepository()
        val viewModel = buildViewModel(
            clock = FixedClock(SATURDAY_AT_TWO),
            fixture = HomeUseCasesFixture(dailyChecks = dailyChecks),
        )
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.ToggleDailyCheck(DailyCheckType.DIET))
        advanceUntilIdle()

        assertEquals(listOf(DailyCheck(FRIDAY, DailyCheckType.DIET, true)), dailyChecks.writes)
        assertEquals(FRIDAY, viewModel.state.value.today)
    }

    @Test
    fun `defining a weekly target navigates to settings`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.DefineWeeklyTarget)
            assertEquals(HomeEffect.NavigateToSettings, awaitItem())
        }
    }

    @Test
    fun `a friday evening routes home to the pre-outing`() = runTest {
        val viewModel = buildViewModel(clock = FixedClock(FRIDAY_EVENING))
        advanceUntilIdle()

        assertEquals(HomeDestination.PRE_OUTING, viewModel.state.value.destination)
    }

    @Test
    fun `the destination follows the window change when the evening begins`() = runTest {
        val clock = VirtualTimeClock(FRIDAY_BEFORE_EVENING, this, FRIDAY_EVENING_ENDS)
        val viewModel = buildViewModel(clock = clock)
        runCurrent()
        assertEquals(HomeDestination.DAILY_CHECKS, viewModel.state.value.destination)

        advanceUntilIdle()

        assertEquals(HomeDestination.PRE_OUTING, viewModel.state.value.destination)
    }

    @Test
    fun `resuming re-evaluates the destination`() = runTest {
        val clock = MutableClock(FRIDAY_BEFORE_EVENING)
        val viewModel = buildViewModel(clock = clock)
        advanceUntilIdle()

        clock.instant = FRIDAY_EVENING
        viewModel.onIntent(HomeIntent.Resume)
        advanceUntilIdle()

        assertEquals(HomeDestination.PRE_OUTING, viewModel.state.value.destination)
    }

    @Test
    fun `showing the daily checks hides the routed screen until the route changes`() = runTest {
        val viewModel = buildViewModel(clock = FixedClock(FRIDAY_EVENING))
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.ShowDailyChecks)
        viewModel.onIntent(HomeIntent.Resume)
        advanceUntilIdle()

        assertEquals(HomeDestination.DAILY_CHECKS, viewModel.state.value.destination)
    }

    @Test
    fun `a pending outing adds the check-in shortcut`() = runTest {
        val outing = Outing(1, FRIDAY, OutingKind.PLANNED, Limits(Money(150_00), 4, LocalTime(1, 30)), checkIn = null)
        val viewModel = buildViewModel(
            fixture = HomeUseCasesFixture(outings = FakeOutingRepository(initialOutings = listOf(outing))),
        )
        advanceUntilIdle()

        assertEquals(HomeShortcut.entries, viewModel.state.value.shortcuts)
    }

    @Test
    fun `without a pending outing the shortcuts skip the check-in`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(
            listOf(HomeShortcut.PRE_OUTING, HomeShortcut.WEEKLY_SUMMARY),
            viewModel.state.value.shortcuts,
        )
    }

    private fun TestScope.keptChecks(vararg checks: DailyCheck): Int {
        val viewModel = buildViewModel(
            fixture = HomeUseCasesFixture(dailyChecks = FakeDailyCheckRepository(checks.toList())),
        )
        advanceUntilIdle()
        return viewModel.state.value.keptChecks
    }

    @Test
    fun `a day with nothing answered has no kept check`() = runTest {
        assertEquals(0, keptChecks())
    }

    @Test
    fun `the four daily checks are the total`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(4, viewModel.state.value.totalChecks)
    }

    @Test
    fun `a done target counts as kept`() = runTest {
        assertEquals(1, keptChecks(DailyCheck(FIXED_DATE, DailyCheckType.GYM, true)))
    }

    @Test
    fun `alcohol recorded as not drank counts as kept`() = runTest {
        assertEquals(1, keptChecks(DailyCheck(FIXED_DATE, DailyCheckType.ALCOHOL, false)))
    }

    @Test
    fun `alcohol recorded as drank does not count as kept`() = runTest {
        val gym = DailyCheck(FIXED_DATE, DailyCheckType.GYM, true)

        assertEquals(1, keptChecks(gym, DailyCheck(FIXED_DATE, DailyCheckType.ALCOHOL, true)))
    }

    @Test
    fun `alcohol left unanswered adds nothing to the kept checks`() = runTest {
        val gym = DailyCheck(FIXED_DATE, DailyCheckType.GYM, true)

        assertEquals(2, keptChecks(gym, DailyCheck(FIXED_DATE, DailyCheckType.DIET, true)))
    }

    @Test
    fun `the next installment is formatted for the board row`() = runTest {
        val card =
            Debt(
                id = 1,
                name = "Tarjeta de crédito",
                balance = Money(5_000_00),
                installment = Money(420_00),
                paymentDay = 15,
            )
        val viewModel = buildViewModel(
            fixture = HomeUseCasesFixture(debts = FakeDebtRepository(initialDebts = listOf(card))),
        )
        advanceUntilIdle()

        assertEquals(
            HomeInstallment("15 JUN", "Tarjeta de crédito", "S/ 420.00"),
            viewModel.state.value.nextInstallment,
        )
    }

    @Test
    fun `without debts there is no next installment`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertNull(viewModel.state.value.nextInstallment)
    }

    private fun alcoholDays(count: Int) = (0 until count).map {
        DailyCheck(FIXED_DATE.minus(DatePeriod(days = it)), DailyCheckType.ALCOHOL, true)
    }

    private fun TestScope.alcoholItem(drinkingDays: Int): DailyCheckItem {
        val setup = Setup(weeklyTargets = mapOf(DailyCheckType.ALCOHOL to 2))
        val viewModel =
            buildViewModel(
                fixture = HomeUseCasesFixture(
                    dailyChecks = FakeDailyCheckRepository(alcoholDays(drinkingDays)),
                    setup = setup,
                ),
            )
        advanceUntilIdle()
        return viewModel.state.value.checks.first { it.type == DailyCheckType.ALCOHOL }
    }

    @Test
    fun `alcohol over its weekly maximum is flagged as a broken limit`() = runTest {
        assertTrue(alcoholItem(drinkingDays = 3).isOverLimit)
    }

    @Test
    fun `alcohol at its weekly maximum is not flagged`() = runTest {
        assertFalse(alcoholItem(drinkingDays = 2).isOverLimit)
    }
}

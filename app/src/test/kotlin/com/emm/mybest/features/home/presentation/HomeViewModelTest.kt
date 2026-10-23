package com.emm.mybest.features.home.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Limits
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeekProgress
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.home.HomeDestination
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import com.emm.mybest.testing.MutableClock
import com.emm.mybest.testing.VirtualTimeClock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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

    private val observeWeightProgress: ObserveWeightProgressUseCase = mockk()
    private val observePhotos: ObservePhotosUseCase = mockk()

    private fun buildViewModel(
        weights: List<WeightEntry> = emptyList(),
        photos: List<ProgressPhoto> = emptyList(),
        clock: Clock = FIXED_CLOCK,
        dailyChecks: FakeDailyCheckRepository = FakeDailyCheckRepository(),
        setup: Setup = Setup(),
        outings: FakeOutingRepository = FakeOutingRepository(),
    ): HomeViewModel {
        every { observeWeightProgress() } returns flowOf(weights)
        every { observePhotos() } returns flowOf(photos)
        val appDayClock = AppDayClock(clock, TimeZone.UTC)
        val fixture = HomeUseCasesFixture(dailyChecks, outings, setup)
        return HomeViewModel(fixture.build(appDayClock, observeWeightProgress, observePhotos), appDayClock)
    }

    @Test
    fun `loaded state carries today and its ISO week number`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(FIXED_DATE, state.today)
        assertEquals(20, state.weekNumber)
    }

    @Test
    fun `empty weights produces null lastWeightKg and null previousWeightKg`() = runTest {
        val viewModel = buildViewModel(weights = emptyList())
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
        val viewModel = buildViewModel(weights = weights)
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
        val viewModel = buildViewModel(photos = photos)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(PhotoType.TRUNK, state.lastPhotoType)
        assertEquals(4, state.lastPhotoDaysAgo)
    }

    @Test
    fun `state lists the four daily checks in order with their done flag`() = runTest {
        val dailyChecks = FakeDailyCheckRepository(listOf(DailyCheck(FIXED_DATE, DailyCheckType.DIET, true)))
        val viewModel = buildViewModel(dailyChecks = dailyChecks)
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
        val viewModel = buildViewModel(dailyChecks = dailyChecks, setup = setup)
        advanceUntilIdle()

        val gym = viewModel.state.value.checks.first { it.type == DailyCheckType.GYM }
        assertEquals(WeekProgress(count = 1, target = 4), gym.progress)
    }

    @Test
    fun `toggling a check persists it for today`() = runTest {
        val dailyChecks = FakeDailyCheckRepository()
        val viewModel = buildViewModel(dailyChecks = dailyChecks)
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.ToggleDailyCheck(DailyCheckType.GYM))
        advanceUntilIdle()

        assertEquals(listOf(DailyCheck(FIXED_DATE, DailyCheckType.GYM, true)), dailyChecks.writes)
        assertTrue(viewModel.state.value.checks.first { it.type == DailyCheckType.GYM }.isDone)
    }

    @Test
    fun `a toggle at Saturday 02 00 writes Friday`() = runTest {
        val dailyChecks = FakeDailyCheckRepository()
        val viewModel = buildViewModel(clock = FixedClock(SATURDAY_AT_TWO), dailyChecks = dailyChecks)
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
        val viewModel = buildViewModel(outings = FakeOutingRepository(initialOutings = listOf(outing)))
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
}

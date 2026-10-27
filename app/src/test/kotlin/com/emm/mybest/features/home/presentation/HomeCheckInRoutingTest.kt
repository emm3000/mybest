package com.emm.mybest.features.home.presentation

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.money.Money
import com.emm.mybest.core.navigation.HomeVisibleDestination
import com.emm.mybest.domain.models.CheckIn
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.usecase.home.HomeDestination
import com.emm.mybest.testing.FakeOutingRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import com.emm.mybest.testing.MutableClock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

private val SATURDAY_MORNING = Instant.parse("2026-10-24T09:00:00Z")
private val SATURDAY_EVENING = Instant.parse("2026-10-24T20:00:00Z")
private val PENDING_FRIDAY = Outing(1, LocalDate(2026, 10, 23), OutingKind.UNPLANNED, limits = null, checkIn = null)
private val FRIDAY_CHECK_IN = CheckIn(Money(200_00), drinks = null, returnTime = null, mood = 2)

class HomeCheckInRoutingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val visibleDestination = HomeVisibleDestination()

    private fun buildViewModel(outings: FakeOutingRepository, clock: Clock): HomeViewModel {
        val appDayClock = AppDayClock(clock, TimeZone.UTC)
        return HomeViewModel(
            HomeUseCasesFixture(outings = outings).build(appDayClock),
            appDayClock,
            visibleDestination,
        )
    }

    private suspend fun TestScope.openAndSaveCheckIn(clock: Clock = FixedClock(SATURDAY_MORNING)): HomeViewModel {
        val outings = FakeOutingRepository(initialOutings = listOf(PENDING_FRIDAY))
        val viewModel = buildViewModel(outings, clock)
        advanceUntilIdle()
        assertEquals(HomeDestination.CHECK_IN, viewModel.state.value.destination)
        outings.saveCheckIn(PENDING_FRIDAY.id, FRIDAY_CHECK_IN)
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `a saved check-in keeps the check-in on screen`() = runTest {
        val viewModel = openAndSaveCheckIn()

        assertEquals(HomeDestination.CHECK_IN, viewModel.state.value.destination)
    }

    @Test
    fun `showing the daily checks after a saved check-in leaves the check-in`() = runTest {
        val viewModel = openAndSaveCheckIn()

        viewModel.onIntent(HomeIntent.ShowDailyChecks)
        viewModel.onIntent(HomeIntent.Resume)
        advanceUntilIdle()

        assertEquals(HomeDestination.DAILY_CHECKS, viewModel.state.value.destination)
    }

    @Test
    fun `the evening replaces a held check-in`() = runTest {
        val clock = MutableClock(SATURDAY_MORNING)
        val viewModel = openAndSaveCheckIn(clock)

        clock.instant = SATURDAY_EVENING
        viewModel.onIntent(HomeIntent.Resume)
        advanceUntilIdle()

        assertEquals(HomeDestination.PRE_OUTING, viewModel.state.value.destination)
    }

    @Test
    fun `home publishes the check-in it shows`() = runTest {
        buildViewModel(FakeOutingRepository(initialOutings = listOf(PENDING_FRIDAY)), FixedClock(SATURDAY_MORNING))
        advanceUntilIdle()

        assertEquals(HomeDestination.CHECK_IN, visibleDestination.destination.value)
    }

    @Test
    fun `a new Home resets what an older Home published while it loads`() = runTest {
        visibleDestination.publish(Any(), HomeDestination.DAILY_CHECKS)

        buildViewModel(FakeOutingRepository(initialOutings = listOf(PENDING_FRIDAY)), FixedClock(SATURDAY_MORNING))

        assertNull(visibleDestination.destination.value)
    }

    @Test
    fun `an older Home going away keeps what a new Home published`() = runTest {
        val olderHome = Any()
        visibleDestination.publish(olderHome, HomeDestination.DAILY_CHECKS)
        buildViewModel(FakeOutingRepository(initialOutings = listOf(PENDING_FRIDAY)), FixedClock(SATURDAY_MORNING))
        advanceUntilIdle()

        visibleDestination.clear(olderHome)

        assertEquals(HomeDestination.CHECK_IN, visibleDestination.destination.value)
    }

    @Test
    fun `home publishes the daily checks after the check-in is dismissed`() = runTest {
        val outings = FakeOutingRepository(initialOutings = listOf(PENDING_FRIDAY))
        val viewModel = buildViewModel(outings, FixedClock(SATURDAY_MORNING))
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.ShowDailyChecks)
        advanceUntilIdle()

        assertEquals(HomeDestination.DAILY_CHECKS, visibleDestination.destination.value)
    }
}

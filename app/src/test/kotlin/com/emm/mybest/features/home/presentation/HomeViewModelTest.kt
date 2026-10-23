package com.emm.mybest.features.home.presentation

import app.cash.turbine.test
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.dailycheck.ObserveDailyChecksUseCase
import com.emm.mybest.domain.usecase.dailycheck.ObserveWeekProgressUseCase
import com.emm.mybest.domain.usecase.dailycheck.ToggleDailyCheckUseCase
import com.emm.mybest.domain.usecase.dailycheck.WeekProgress
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.testing.FakeDailyCheckRepository
import com.emm.mybest.testing.FakeSetupRepository
import com.emm.mybest.testing.FixedClock
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
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
    ): HomeViewModel {
        every { observeWeightProgress() } returns flowOf(weights)
        every { observePhotos() } returns flowOf(photos)
        val setupRepository = FakeSetupRepository(setup)
        return HomeViewModel(
            useCases = HomeUseCases(
                observeWeightProgress = observeWeightProgress,
                observePhotos = observePhotos,
                observeDailyChecks = ObserveDailyChecksUseCase(dailyChecks),
                observeWeekProgress = ObserveWeekProgressUseCase(dailyChecks, setupRepository),
                toggleDailyCheck = ToggleDailyCheckUseCase(dailyChecks),
            ),
            appDayClock = AppDayClock(clock, TimeZone.UTC),
        )
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
}

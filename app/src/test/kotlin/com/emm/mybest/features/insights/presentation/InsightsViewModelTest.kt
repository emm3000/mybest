package com.emm.mybest.features.insights.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.InsightsData
import com.emm.mybest.domain.models.InsightsRecommendation
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.domain.models.InsightsRecommendationKind
import com.emm.mybest.domain.models.PeriodLabel
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.GetInsightsUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

private val FIXED_CLOCK = object : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(1_779_624_000L)
}

class InsightsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getInsightsUseCase = mockk<GetInsightsUseCase>()

    private val keepRoutineRecommendation = InsightsRecommendation(
        kind = InsightsRecommendationKind.KEEP_ROUTINE,
        action = InsightsRecommendationAction.KEEP_ROUTINE,
    )

    private val sampleInsightsData = InsightsData(
        weightEntries = listOf(
            WeightEntry(id = "w1", date = LocalDate(2026, 1, 1), weight = 80f),
            WeightEntry(id = "w2", date = LocalDate(2026, 2, 1), weight = 75f),
        ),
        period = PeriodLabel.Range(LocalDate(2026, 1, 1), LocalDate(2026, 2, 1)),
        totalWeightLost = 5f,
        currentWeight = 75f,
        initialWeight = 80f,
        photoCount = 4,
        recommendation = keepRoutineRecommendation,
    )

    private fun buildViewModel(): InsightsViewModel {
        every { getInsightsUseCase(any()) } returns flowOf(sampleInsightsData)
        return InsightsViewModel(getInsightsUseCase, FIXED_CLOCK)
    }

    @Test
    fun `initial state has isLoading true`() = runTest {
        every { getInsightsUseCase(any()) } returns flowOf(sampleInsightsData)
        val viewModel = InsightsViewModel(getInsightsUseCase, FIXED_CLOCK)

        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loaded state maps all InsightsData fields correctly`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            assertEquals(2, state.weightHistory.size)
            assertEquals(5f, state.totalWeightLost, 0.001f)
            assertEquals(75f, state.currentWeight, 0.001f)
            assertEquals(80f, state.initialWeight, 0.001f)
            assertEquals(4, state.photoCount)
            assertTrue(state.hasRecommendation)
            assertEquals(InsightsRecommendationAction.KEEP_ROUTINE, state.recommendationAction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `period label is rendered for range`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            val state = awaitItem()
            assertTrue(state.periodLabel.contains("enero"))
            assertTrue(state.periodLabel.contains("febrero"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `canComparePhotos is true when photoCount is at least 2`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertTrue(loaded.canComparePhotos)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `canComparePhotos is false when photoCount is below 2`() = runTest {
        val data = sampleInsightsData.copy(photoCount = 1)
        every { getInsightsUseCase(any()) } returns flowOf(data)
        val viewModel = InsightsViewModel(getInsightsUseCase, FIXED_CLOCK)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertFalse(loaded.canComparePhotos)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnCompareClick emits NavigateToCompare effect`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnCompareClick)
            assertEquals(InsightsEffect.NavigateToCompare, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnHistoryClick emits NavigateToHistory effect`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnHistoryClick)
            assertEquals(InsightsEffect.NavigateToHistory, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnAddWeightClick emits NavigateToAddWeight effect`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnAddWeightClick)
            assertEquals(InsightsEffect.NavigateToAddWeight, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRecommendationActionClick emits NavigateByRecommendation with action from state`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnRecommendationActionClick)
            advanceUntilIdle()
            assertEquals(
                InsightsEffect.NavigateByRecommendation(InsightsRecommendationAction.KEEP_ROUTINE),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRecommendationActionClick with ADJUST_WEIGHT_PLAN emits correct action`() = runTest {
        val adjustRecommendation = InsightsRecommendation(
            kind = InsightsRecommendationKind.ADJUST_WEEKLY_PLAN,
            action = InsightsRecommendationAction.ADJUST_WEIGHT_PLAN,
        )
        val data = sampleInsightsData.copy(recommendation = adjustRecommendation)
        every { getInsightsUseCase(any()) } returns flowOf(data)
        val viewModel = InsightsViewModel(getInsightsUseCase, FIXED_CLOCK)

        viewModel.state.test {
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnRecommendationActionClick)
            advanceUntilIdle()
            assertEquals(
                InsightsEffect.NavigateByRecommendation(InsightsRecommendationAction.ADJUST_WEIGHT_PLAN),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error from use case sets errorMessage and clears isLoading`() = runTest {
        every { getInsightsUseCase(any()) } returns flow { throw IllegalStateException("network error") }
        val viewModel = InsightsViewModel(getInsightsUseCase, FIXED_CLOCK)

        viewModel.state.test {
            awaitItem()
            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertNotNull(errorState.errorMessage)
            assertEquals("network error", errorState.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRecommendationActionClick does nothing when recommendation is null`() = runTest {
        every { getInsightsUseCase(any()) } returns flow { }
        val viewModel = InsightsViewModel(getInsightsUseCase, FIXED_CLOCK)

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnRecommendationActionClick)
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state propagates three new weight metrics from InsightsData`() = runTest {
        val data = sampleInsightsData.copy(
            deltaWeightKg = -4.2f,
            deltaWeightPercent = -5.1f,
            kgPerDayRate14d = -0.12f,
        )
        every { getInsightsUseCase(any()) } returns flowOf(data)
        val viewModel = InsightsViewModel(getInsightsUseCase, FIXED_CLOCK)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertEquals(-4.2f, loaded.deltaWeightKg!!, 0.001f)
            assertEquals(-5.1f, loaded.deltaWeightPercent!!, 0.001f)
            assertEquals(-0.12f, loaded.kgPerDayRate14d!!, 0.001f)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

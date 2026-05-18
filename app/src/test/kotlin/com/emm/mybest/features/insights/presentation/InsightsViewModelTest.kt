package com.emm.mybest.features.insights.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.InsightsData
import com.emm.mybest.domain.models.InsightsRecommendation
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.GetInsightsUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getInsightsUseCase = mockk<GetInsightsUseCase>()

    private val keepRoutineRecommendation = InsightsRecommendation(
        title = "Mantén el ritmo",
        description = "desc",
        actionLabel = "Sostén",
        action = InsightsRecommendationAction.KEEP_ROUTINE,
    )

    private val sampleInsightsData = InsightsData(
        weightEntries = listOf(
            WeightEntry(id = "w1", date = LocalDate(2026, 1, 1), weight = 80f),
            WeightEntry(id = "w2", date = LocalDate(2026, 2, 1), weight = 75f),
        ),
        periodLabel = "Datos del 1 de enero, 2026 al 1 de febrero, 2026",
        totalWeightLost = 5f,
        currentWeight = 75f,
        initialWeight = 80f,
        photoCount = 4,
        recommendation = keepRoutineRecommendation,
    )

    private fun buildViewModel(): InsightsViewModel {
        every { getInsightsUseCase() } returns flowOf(sampleInsightsData)
        return InsightsViewModel(getInsightsUseCase)
    }

    @Test
    fun `initial state has isLoading true`() = runTest {
        every { getInsightsUseCase() } returns flowOf(sampleInsightsData)
        val viewModel = InsightsViewModel(getInsightsUseCase)

        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loaded state maps all InsightsData fields correctly`() = runTest {
        val viewModel = buildViewModel()

        viewModel.state.test {
            awaitItem() // loading
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            assertEquals(2, state.weightHistory.size)
            assertEquals(5f, state.totalWeightLost, 0.001f)
            assertEquals(75f, state.currentWeight, 0.001f)
            assertEquals(80f, state.initialWeight, 0.001f)
            assertEquals(4, state.photoCount)
            assertEquals(keepRoutineRecommendation, state.recommendation)
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
        every { getInsightsUseCase() } returns flowOf(data)
        val viewModel = InsightsViewModel(getInsightsUseCase)

        viewModel.state.test {
            awaitItem()
            val loaded = awaitItem()
            assertFalse(loaded.canComparePhotos)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnBackClick emits NavigateBack effect`() = runTest {
        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnBackClick)
            assertEquals(InsightsEffect.NavigateBack, awaitItem())
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
    fun `OnRecommendationActionClick emits NavigateByRecommendation with action from state`() = runTest {
        val viewModel = buildViewModel()

        // Wait for state to be loaded (recommendation available)
        viewModel.state.test {
            awaitItem() // loading
            awaitItem() // loaded — recommendation is now in state
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
        val adjustRecommendation = keepRoutineRecommendation.copy(
            action = InsightsRecommendationAction.ADJUST_WEIGHT_PLAN,
        )
        val data = sampleInsightsData.copy(recommendation = adjustRecommendation)
        every { getInsightsUseCase() } returns flowOf(data)
        val viewModel = InsightsViewModel(getInsightsUseCase)

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
        every { getInsightsUseCase() } returns flow { throw IllegalStateException("network error") }
        val viewModel = InsightsViewModel(getInsightsUseCase)

        viewModel.state.test {
            awaitItem() // isLoading=true
            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertNotNull(errorState.errorMessage)
            assertEquals("network error", errorState.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRecommendationActionClick does nothing when recommendation is null`() = runTest {
        // Use a use case that never emits so state stays at isLoading=true (recommendation=null)
        every { getInsightsUseCase() } returns flow { /* never emits */ }
        val viewModel = InsightsViewModel(getInsightsUseCase)

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnRecommendationActionClick)
            advanceUntilIdle()
            // null recommendation → no effect should be emitted
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}

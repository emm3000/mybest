package com.emm.mybest.features.insights.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.InsightsData
import com.emm.mybest.domain.models.InsightsRecommendation
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.domain.usecase.GetInsightsUseCase
import com.emm.mybest.testing.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getInsightsUseCase = mockk<GetInsightsUseCase>()

    @Test
    fun `state maps insights data from use case`() = runTest {
        val expected = InsightsData(
            weightEntries = emptyList(),
            periodLabel = "Datos del 1 de marzo, 2026 al 8 de marzo, 2026",
            habitConsistency = 0.75f,
            totalWeightLost = 4f,
            currentWeight = 76f,
            initialWeight = 80f,
            exerciseDays = 10,
            healthyEatingDays = 8,
            photoCount = 3,
            recommendation = InsightsRecommendation(
                title = "Mantén el ritmo",
                description = "Texto",
                actionLabel = "Acción",
                action = InsightsRecommendationAction.KEEP_ROUTINE,
            ),
        )
        every { getInsightsUseCase.invoke() } returns flowOf(expected)

        val viewModel = InsightsViewModel(getInsightsUseCase)
        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            val state = awaitItem()
            assertEquals(expected.weightEntries, state.weightHistory)
            assertEquals(expected.periodLabel, state.periodLabel)
            assertEquals(expected.habitConsistency, state.habitConsistency)
            assertEquals(expected.totalWeightLost, state.totalWeightLost)
            assertEquals(expected.currentWeight, state.currentWeight)
            assertEquals(expected.initialWeight, state.initialWeight)
            assertEquals(expected.exerciseDays, state.exerciseDays)
            assertEquals(expected.healthyEatingDays, state.healthyEatingDays)
            assertEquals(expected.photoCount, state.photoCount)
            assertEquals(expected.recommendation.title, state.recommendation?.title)
            assertEquals(true, state.canComparePhotos)
            assertEquals(false, state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnBackClick emits NavigateBack effect`() = runTest {
        every { getInsightsUseCase.invoke() } returns flowOf(
            InsightsData(
                emptyList(),
                "Sin periodo disponible aún.",
                0f,
                0f,
                0f,
                0f,
                0,
                0,
                0,
                InsightsRecommendation("x", "y", "z", InsightsRecommendationAction.KEEP_ROUTINE),
            ),
        )
        val viewModel = InsightsViewModel(getInsightsUseCase)

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnBackClick)
            assertEquals(InsightsEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnCompareClick emits NavigateToCompare effect`() = runTest {
        every { getInsightsUseCase.invoke() } returns flowOf(
            InsightsData(
                emptyList(),
                "Sin periodo disponible aún.",
                0f,
                0f,
                0f,
                0f,
                0,
                0,
                2,
                InsightsRecommendation("x", "y", "z", InsightsRecommendationAction.ADD_PROGRESS_PHOTO),
            ),
        )
        val viewModel = InsightsViewModel(getInsightsUseCase)

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnCompareClick)
            assertEquals(InsightsEffect.NavigateToCompare, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRecommendationActionClick emits NavigateByRecommendation effect`() = runTest {
        every { getInsightsUseCase.invoke() } returns flowOf(
            InsightsData(
                emptyList(),
                "Datos del 1 de marzo, 2026 al 2 de marzo, 2026",
                0.5f,
                1f,
                79f,
                80f,
                3,
                2,
                1,
                InsightsRecommendation(
                    "Refuerza la constancia",
                    "Texto",
                    "Prioriza un hábito clave",
                    InsightsRecommendationAction.PRIORITIZE_HABIT,
                ),
            ),
        )
        val viewModel = InsightsViewModel(getInsightsUseCase)
        viewModel.state.test {
            awaitItem()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnRecommendationActionClick)
            assertEquals(
                InsightsEffect.NavigateByRecommendation(InsightsRecommendationAction.PRIORITIZE_HABIT),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}

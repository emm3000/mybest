package com.emm.mybest.features.insights.presentation

import app.cash.turbine.test
import com.emm.mybest.domain.models.InsightsData
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
            habitConsistency = 0.75f,
            totalWeightLost = 4f,
            currentWeight = 76f,
            initialWeight = 80f,
            exerciseDays = 10,
            healthyEatingDays = 8,
            photoCount = 3,
        )
        every { getInsightsUseCase.invoke() } returns flowOf(expected)

        val viewModel = InsightsViewModel(getInsightsUseCase)
        viewModel.state.test {
            assertEquals(true, awaitItem().isLoading)
            val state = awaitItem()
            assertEquals(expected.weightEntries, state.weightHistory)
            assertEquals(expected.habitConsistency, state.habitConsistency)
            assertEquals(expected.totalWeightLost, state.totalWeightLost)
            assertEquals(expected.currentWeight, state.currentWeight)
            assertEquals(expected.initialWeight, state.initialWeight)
            assertEquals(expected.exerciseDays, state.exerciseDays)
            assertEquals(expected.healthyEatingDays, state.healthyEatingDays)
            assertEquals(expected.photoCount, state.photoCount)
            assertEquals(true, state.canComparePhotos)
            assertEquals(false, state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnBackClick emits NavigateBack effect`() = runTest {
        every { getInsightsUseCase.invoke() } returns flowOf(
            InsightsData(emptyList(), 0f, 0f, 0f, 0f, 0, 0, 0),
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
            InsightsData(emptyList(), 0f, 0f, 0f, 0f, 0, 0, 2),
        )
        val viewModel = InsightsViewModel(getInsightsUseCase)

        viewModel.effect.test {
            viewModel.onIntent(InsightsIntent.OnCompareClick)
            assertEquals(InsightsEffect.NavigateToCompare, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

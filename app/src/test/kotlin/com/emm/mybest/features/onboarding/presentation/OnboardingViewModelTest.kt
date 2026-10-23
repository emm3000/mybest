package com.emm.mybest.features.onboarding.presentation

import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.onboarding.ObserveOnboardingCompletedUseCase
import com.emm.mybest.domain.usecase.onboarding.SetOnboardingCompletedUseCase
import com.emm.mybest.features.onboarding.presentation.OnboardingPrimaryAction.FINISH
import com.emm.mybest.features.onboarding.presentation.OnboardingPrimaryAction.NEXT
import com.emm.mybest.features.onboarding.presentation.OnboardingStep.CHECKS
import com.emm.mybest.features.onboarding.presentation.OnboardingStep.MONEY
import com.emm.mybest.features.onboarding.presentation.OnboardingStep.OUTINGS
import com.emm.mybest.features.onboarding.presentation.OnboardingStep.WEIGHT
import com.emm.mybest.features.onboarding.presentation.OnboardingStep.WELCOME
import com.emm.mybest.testing.FakeUserPreferencesRepository
import com.emm.mybest.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private const val STEP_COUNT = 5

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(repository: UserPreferencesRepository) = OnboardingViewModel(
        observeOnboardingCompleted = ObserveOnboardingCompletedUseCase(repository),
        setOnboardingCompleted = SetOnboardingCompletedUseCase(repository),
    )

    @Test
    fun `gate is loading before the flag is read`() {
        val viewModel = buildViewModel(FakeUserPreferencesRepository())

        assertEquals(OnboardingGate.LOADING, viewModel.state.value.gate)
    }

    @Test
    fun `missing flag requires the intro from the welcome step`() = runTest {
        val viewModel = buildViewModel(FakeUserPreferencesRepository(isOnboardingCompleted = false))
        advanceUntilIdle()

        assertEquals(OnboardingGate.REQUIRED, viewModel.state.value.gate)
        assertEquals(OnboardingStep.WELCOME, viewModel.state.value.step)
    }

    @Test
    fun `completed flag skips the intro`() = runTest {
        val viewModel = buildViewModel(FakeUserPreferencesRepository(isOnboardingCompleted = true))
        advanceUntilIdle()

        assertEquals(OnboardingGate.DONE, viewModel.state.value.gate)
    }

    @Test
    fun `next walks the steps in order`() = runTest {
        val viewModel = buildViewModel(FakeUserPreferencesRepository())
        advanceUntilIdle()

        val visited = List(STEP_COUNT - 1) {
            viewModel.onIntent(OnboardingIntent.Next)
            advanceUntilIdle()
            viewModel.state.value.step
        }

        assertEquals(listOf(CHECKS, OUTINGS, MONEY, WEIGHT), visited)
    }

    @Test
    fun `back walks the steps in reverse and stops at welcome`() = runTest {
        val viewModel = buildViewModel(FakeUserPreferencesRepository())
        advanceUntilIdle()
        repeat(STEP_COUNT - 1) { viewModel.onIntent(OnboardingIntent.Next) }

        val visited = List(STEP_COUNT) {
            viewModel.onIntent(OnboardingIntent.Back)
            advanceUntilIdle()
            viewModel.state.value.step
        }

        assertEquals(listOf(MONEY, OUTINGS, CHECKS, WELCOME, WELCOME), visited)
    }

    @Test
    fun `state exposes position, total and the primary action of each step`() = runTest {
        val viewModel = buildViewModel(FakeUserPreferencesRepository())
        advanceUntilIdle()

        val seen = List(STEP_COUNT) { index ->
            if (index > 0) viewModel.onIntent(OnboardingIntent.Next)
            advanceUntilIdle()
            viewModel.state.value
        }

        assertEquals(listOf(1, 2, 3, 4, 5), seen.map { it.position })
        assertEquals(listOf(5, 5, 5, 5, 5), seen.map { it.total })
        assertEquals(listOf(NEXT, NEXT, NEXT, NEXT, FINISH), seen.map { it.primaryAction })
    }

    @Test
    fun `next on the last step stores the flag and finishes the intro`() = runTest {
        val repository = FakeUserPreferencesRepository()
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        repeat(STEP_COUNT) { viewModel.onIntent(OnboardingIntent.Next) }
        advanceUntilIdle()

        assertEquals(true, repository.onboardingCompleted.first())
        assertEquals(OnboardingGate.DONE, viewModel.state.value.gate)
    }

    @Test
    fun `skip on any step stores the flag and finishes the intro`() = runTest {
        val outcomes = List(STEP_COUNT) { stepsWalked ->
            val repository = FakeUserPreferencesRepository()
            val viewModel = buildViewModel(repository)
            advanceUntilIdle()
            repeat(stepsWalked) { viewModel.onIntent(OnboardingIntent.Next) }
            val skippedStep = viewModel.state.value.step
            viewModel.onIntent(OnboardingIntent.Skip)
            advanceUntilIdle()
            Triple(skippedStep, repository.onboardingCompleted.first(), viewModel.state.value.gate)
        }

        assertEquals(OnboardingStep.entries.map { Triple(it, true, OnboardingGate.DONE) }, outcomes)
    }

    @Test
    fun `replay requires the intro again from the welcome step`() = runTest {
        val repository = FakeUserPreferencesRepository()
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()
        repeat(2) { viewModel.onIntent(OnboardingIntent.Next) }
        assertEquals(OUTINGS, viewModel.state.value.step)
        viewModel.onIntent(OnboardingIntent.Skip)
        advanceUntilIdle()
        val seen = mutableListOf<OnboardingState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.toList(seen) }

        repository.updateOnboardingCompleted(false)
        advanceUntilIdle()

        assertEquals(listOf(OnboardingGate.DONE, OnboardingGate.REQUIRED), seen.map { it.gate })
        assertEquals(OnboardingState(OnboardingGate.REQUIRED, WELCOME), seen.last())
    }

    @Test
    fun `only steps after welcome handle back`() = runTest {
        val viewModel = buildViewModel(FakeUserPreferencesRepository())
        advanceUntilIdle()

        val canGoBack = List(STEP_COUNT) { index ->
            if (index > 0) viewModel.onIntent(OnboardingIntent.Next)
            advanceUntilIdle()
            viewModel.state.value.canGoBack
        }

        assertEquals(listOf(false, true, true, true, true), canGoBack)
    }

    @Test
    fun `a failed flag read opens the app`() = runTest {
        val viewModel = buildViewModel(FailingPreferencesRepository(flow { throw IOException(READ_FAILURE) }))
        advanceUntilIdle()

        assertEquals(OnboardingGate.DONE, viewModel.state.value.gate)
    }

    @Test
    fun `a failed flag write keeps the intro on screen`() = runTest {
        val viewModel = buildViewModel(FailingPreferencesRepository(flowOf(false)))
        advanceUntilIdle()

        viewModel.onIntent(OnboardingIntent.Skip)
        advanceUntilIdle()

        assertEquals(OnboardingGate.REQUIRED, viewModel.state.value.gate)
    }
}

private const val READ_FAILURE = "read failed"

private class FailingPreferencesRepository(
    override val onboardingCompleted: Flow<Boolean>,
) : UserPreferencesRepository {
    override val notificationsEnabled: Flow<Boolean> = flowOf(true)

    override suspend fun updateNotificationsEnabled(enabled: Boolean) = Unit

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        throw IOException("write failed")
    }
}

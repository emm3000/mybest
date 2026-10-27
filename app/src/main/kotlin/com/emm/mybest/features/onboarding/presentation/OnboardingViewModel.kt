package com.emm.mybest.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.coroutines.attempt
import com.emm.mybest.domain.usecase.onboarding.ObserveOnboardingCompletedUseCase
import com.emm.mybest.domain.usecase.onboarding.SetOnboardingCompletedUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    observeOnboardingCompleted: ObserveOnboardingCompletedUseCase,
    private val setOnboardingCompleted: SetOnboardingCompletedUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        observeOnboardingCompleted()
            .catch { error -> if (error is CancellationException) throw error else emit(true) }
            .distinctUntilChanged()
            .onEach { isCompleted -> _state.value = gateStateFor(isCompleted) }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.Next -> goForward()
            OnboardingIntent.Back -> goBack()
            OnboardingIntent.Skip -> complete()
        }
    }

    private fun goForward() {
        val next = OnboardingStep.entries.getOrNull(_state.value.step.ordinal + 1)
        if (next == null) complete() else _state.update { it.copy(step = next) }
    }

    private fun goBack() {
        _state.update { it.copy(step = OnboardingStep.entries.getOrElse(it.step.ordinal - 1) { _ -> it.step }) }
    }

    private fun complete() {
        viewModelScope.launch {
            attempt { setOnboardingCompleted(true) }
        }
    }

    private fun gateStateFor(isCompleted: Boolean): OnboardingState = if (isCompleted) {
        OnboardingState(gate = OnboardingGate.DONE)
    } else {
        OnboardingState(gate = OnboardingGate.REQUIRED, step = OnboardingStep.WELCOME)
    }
}

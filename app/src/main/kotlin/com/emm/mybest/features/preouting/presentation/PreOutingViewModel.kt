package com.emm.mybest.features.preouting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.usecase.preouting.ObservePreOutingSnapshotUseCase
import com.emm.mybest.domain.usecase.preouting.PlanOutingResult
import com.emm.mybest.domain.usecase.preouting.PreOutingDecisionUseCases
import com.emm.mybest.domain.usecase.preouting.RecordWonNightResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreOutingViewModel(
    observeSnapshot: ObservePreOutingSnapshotUseCase,
    purchaseGoalRepository: PurchaseGoalRepository,
    private val decisions: PreOutingDecisionUseCases,
) : ViewModel() {

    private val _effect = effectFlow<PreOutingEffect>()
    val effect: SharedFlow<PreOutingEffect> = _effect.asSharedFlow()

    private val form = MutableStateFlow(PreOutingForm())

    val state: StateFlow<PreOutingState> = combine(
        combine(observeSnapshot(), purchaseGoalRepository.observeGoal().map { it?.name }) { snapshot, goalName ->
            snapshot.toState(goalName)
        },
        decisions.observeTonight(),
        form,
        PreOutingState::withDecision,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = PreOutingState(),
    )

    fun onIntent(intent: PreOutingIntent) {
        when (intent) {
            is PreOutingIntent.OnPromptClick -> _effect.tryEmit(PreOutingEffect.OpenSettings)
            PreOutingIntent.OnStayInClick -> stayIn()
            PreOutingIntent.OnConfirmOutingClick -> confirmOuting()
            else -> editForm(intent)
        }
    }

    private fun editForm(intent: PreOutingIntent) {
        form.update { it.reduce(intent) }
    }

    private fun stayIn() {
        viewModelScope.launch {
            if (decisions.recordWonNight() == RecordWonNightResult.DayAlreadyDecided) {
                _effect.tryEmit(PreOutingEffect.DayAlreadyDecided)
            }
        }
    }

    private fun confirmOuting() {
        val limits = form.value.takeIf { it.canConfirm }?.toLimits() ?: return
        viewModelScope.launch {
            when (decisions.planOuting(limits)) {
                PlanOutingResult.Planned -> form.value = PreOutingForm()
                PlanOutingResult.DayAlreadyDecided -> _effect.tryEmit(PreOutingEffect.DayAlreadyDecided)
                PlanOutingResult.NonPositiveMoney, PlanOutingResult.NegativeDrinks -> Unit
            }
        }
    }
}

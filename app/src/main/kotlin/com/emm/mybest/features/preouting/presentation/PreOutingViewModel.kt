package com.emm.mybest.features.preouting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.coroutines.attempt
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.usecase.preouting.ObservePreOutingSnapshotUseCase
import com.emm.mybest.domain.usecase.preouting.PlanOutingResult
import com.emm.mybest.domain.usecase.preouting.PreOutingDecisionUseCases
import com.emm.mybest.domain.usecase.preouting.RecordWonNightResult
import kotlinx.coroutines.Job
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
    private var confirmJob: Job? = null
    private var stayInJob: Job? = null

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
            is PreOutingIntent.OnPromptClick -> _effect.tryEmit(intent.prompt.destination())
            PreOutingIntent.OnStayInClick -> stayIn()
            PreOutingIntent.OnConfirmOutingClick -> confirmOuting()
            else -> editForm(intent)
        }
    }

    private fun editForm(intent: PreOutingIntent) {
        form.update { it.reduce(intent) }
    }

    private fun stayIn() {
        if (stayInJob?.isActive == true) return
        stayInJob = viewModelScope.launch {
            attempt { decisions.recordWonNight() }
                .onSuccess(::onStayInResult)
                .onFailure { _effect.emit(PreOutingEffect.ActionFailed) }
        }
    }

    private fun onStayInResult(result: RecordWonNightResult) {
        when (result) {
            is RecordWonNightResult.Recorded -> Unit
            RecordWonNightResult.NotWonNightDay -> _effect.tryEmit(PreOutingEffect.NotWonNightDay)
            RecordWonNightResult.DayAlreadyDecided -> _effect.tryEmit(PreOutingEffect.DayAlreadyDecided)
        }
    }

    private fun confirmOuting() {
        if (confirmJob?.isActive == true) return
        val limits = form.value.takeIf { it.canConfirm }?.toLimits() ?: return
        confirmJob = viewModelScope.launch {
            attempt { decisions.planOuting(limits) }
                .onSuccess(::onPlanResult)
                .onFailure { _effect.emit(PreOutingEffect.ActionFailed) }
        }
    }

    private fun onPlanResult(result: PlanOutingResult) {
        when (result) {
            PlanOutingResult.Planned -> form.value = PreOutingForm()
            PlanOutingResult.DayAlreadyDecided -> _effect.tryEmit(PreOutingEffect.DayAlreadyDecided)
            PlanOutingResult.NonPositiveMoney,
            PlanOutingResult.NegativeDrinks,
            PlanOutingResult.ReturnTimeInDaytime,
            -> Unit
        }
    }
}

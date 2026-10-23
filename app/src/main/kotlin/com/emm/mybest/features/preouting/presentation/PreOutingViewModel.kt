package com.emm.mybest.features.preouting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.repository.PurchaseGoalRepository
import com.emm.mybest.domain.usecase.preouting.ObservePreOutingSnapshotUseCase
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PreOutingViewModel(
    observeSnapshot: ObservePreOutingSnapshotUseCase,
    purchaseGoalRepository: PurchaseGoalRepository,
) : ViewModel() {

    private val _effect = effectFlow<PreOutingEffect>()
    val effect: SharedFlow<PreOutingEffect> = _effect.asSharedFlow()

    val state: StateFlow<PreOutingState> = combine(
        observeSnapshot(),
        purchaseGoalRepository.observeGoal().map { it?.name },
    ) { snapshot, goalName -> snapshot.toState(goalName) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = PreOutingState(),
        )

    fun onIntent(intent: PreOutingIntent) {
        when (intent) {
            is PreOutingIntent.OnPromptClick -> _effect.tryEmit(PreOutingEffect.OpenSettings)
        }
    }
}

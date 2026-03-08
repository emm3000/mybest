package com.emm.mybest.features.insights.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.usecase.GetInsightsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InsightsState(
    val weightHistory: List<WeightEntry> = emptyList(),
    val habitConsistency: Float = 0f,
    val totalWeightLost: Float = 0f,
    val currentWeight: Float = 0f,
    val initialWeight: Float = 0f,
    val exerciseDays: Int = 0,
    val healthyEatingDays: Int = 0,
    val isLoading: Boolean = true
)

sealed class InsightsIntent {
    object OnBackClick : InsightsIntent()
    object OnCompareClick : InsightsIntent()
}

sealed class InsightsEffect {
    object NavigateBack : InsightsEffect()
    object NavigateToCompare : InsightsEffect()
}

class InsightsViewModel(
    getInsightsUseCase: GetInsightsUseCase
) : ViewModel() {

    private val _effect = MutableSharedFlow<InsightsEffect>()
    val effect = _effect.asSharedFlow()

    val state: StateFlow<InsightsState> = getInsightsUseCase()
        .map { data ->
            InsightsState(
                weightHistory = data.weightEntries,
                habitConsistency = data.habitConsistency,
                totalWeightLost = data.totalWeightLost,
                currentWeight = data.currentWeight,
                initialWeight = data.initialWeight,
                exerciseDays = data.exerciseDays,
                healthyEatingDays = data.healthyEatingDays,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_STOP_TIMEOUT),
            initialValue = InsightsState(isLoading = true)
        )

    fun onIntent(intent: InsightsIntent) {
        viewModelScope.launch {
            when (intent) {
                InsightsIntent.OnBackClick -> _effect.emit(InsightsEffect.NavigateBack)
                InsightsIntent.OnCompareClick -> _effect.emit(InsightsEffect.NavigateToCompare)
            }
        }
    }

    companion object {
        private const val FLOW_STOP_TIMEOUT = 5000L
    }
}

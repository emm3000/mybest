package com.emm.mybest.features.summary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.usecase.summary.ObserveWeeklySummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class SummaryViewModel(
    observeSummary: ObserveWeeklySummaryUseCase,
    appDayClock: AppDayClock,
) : ViewModel() {

    private val weeksBack = MutableStateFlow(0)

    private val _effect = effectFlow<SummaryEffect>()
    val effect: SharedFlow<SummaryEffect> = _effect.asSharedFlow()

    val state: StateFlow<SummaryState> = combine(appDayClock.observeToday(), weeksBack, ::SummaryWeek)
        .flatMapLatest { week -> observeSummary(week.appDay).map { it.toState(week) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = SummaryState(),
        )

    fun onIntent(intent: SummaryIntent) {
        when (intent) {
            SummaryIntent.OnPreviousWeekClick -> weeksBack.update { it + 1 }
            SummaryIntent.OnNextWeekClick -> weeksBack.update { (it - 1).coerceAtLeast(0) }
            SummaryIntent.OnSetupPromptClick -> _effect.tryEmit(SummaryEffect.OpenSettings)
        }
    }
}

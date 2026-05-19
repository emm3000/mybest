package com.emm.mybest.features.diet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.diet.UpsertMealUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek

class MealPlanViewModel(
    private val getPlan: GetWeeklyMealPlanUseCase,
    private val upsertMeal: UpsertMealUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MealPlanState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MealPlanEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST,
    )
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            getPlan().collect { plan ->
                val byDay = DayOfWeek.entries.associateWith { day ->
                    MealType.entries.associateWith { type ->
                        plan.entryFor(day, type)?.description.orEmpty()
                    }
                }
                _state.update { it.copy(isLoading = false, entries = byDay) }
            }
        }
    }

    fun onIntent(intent: MealPlanIntent) {
        when (intent) {
            is MealPlanIntent.StartEdit -> _state.update {
                val current = it.entries[intent.day]?.get(intent.type).orEmpty()
                it.copy(editing = EditingMeal(intent.day, intent.type, current))
            }
            is MealPlanIntent.UpdateDraft -> _state.update {
                it.copy(editing = it.editing?.copy(draftDescription = intent.description))
            }
            MealPlanIntent.SaveMeal -> saveMeal()
            MealPlanIntent.CancelEdit -> _state.update { it.copy(editing = null) }
        }
    }

    private fun saveMeal() {
        val editing = _state.value.editing ?: return
        viewModelScope.launch {
            upsertMeal(MealPlanEntry(editing.day, editing.type, editing.draftDescription.trim()))
            _state.update { it.copy(editing = null) }
            _effects.emit(MealPlanEffect.DismissSheet)
        }
    }
}

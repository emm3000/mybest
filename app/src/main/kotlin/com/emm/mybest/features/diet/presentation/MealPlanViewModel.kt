package com.emm.mybest.features.diet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.diet.UpsertMealUseCase
import com.emm.mybest.features.diet.presentation.edit.EditingMealDraft
import com.emm.mybest.features.diet.presentation.edit.MAX_MEAL_DESCRIPTION_LENGTH
import com.emm.mybest.features.diet.presentation.edit.toDailySlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class MealPlanViewModel(
    private val getPlan: GetWeeklyMealPlanUseCase,
    private val upsertMeal: UpsertMealUseCase,
    clock: Clock = Clock.System,
) : ViewModel() {

    private val _state = MutableStateFlow(
        MealPlanState(today = clock.todayIn(TimeZone.currentSystemDefault())),
    )
    val state = _state.asStateFlow()

    private val _effects = effectFlow<MealPlanEffect>()
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
            is MealPlanIntent.StartEdit -> handleStartEdit(intent.day, intent.type)
            is MealPlanIntent.UpdateDraft -> handleUpdateDraft(intent.description)
            MealPlanIntent.SaveMeal -> saveMeal()
            MealPlanIntent.CancelEdit -> _state.update { it.copy(editing = null) }
        }
    }

    private fun handleStartEdit(day: DayOfWeek, type: MealType) {
        val current = _state.value.entries[day]?.get(type).orEmpty()
        val draft = EditingMealDraft(
            day = day,
            slot = type.toDailySlot(),
            type = type,
            description = current,
        )
        _state.update { it.copy(editing = draft) }
    }

    private fun handleUpdateDraft(description: String) {
        val current = _state.value.editing ?: return
        _state.update {
            it.copy(editing = current.copy(description = description.take(MAX_MEAL_DESCRIPTION_LENGTH)))
        }
    }

    private fun saveMeal() {
        val editing = _state.value.editing ?: return
        viewModelScope.launch {
            upsertMeal(MealPlanEntry(editing.day, editing.type, editing.description.trim()))
            _state.update { it.copy(editing = null) }
            _effects.emit(MealPlanEffect.DismissSheet)
        }
    }
}

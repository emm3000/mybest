package com.emm.mybest.features.exercise.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
import com.emm.mybest.domain.usecase.exercise.UpsertExerciseRoutineUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek

class ExercisePlanViewModel(
    private val getPlan: GetWeeklyExercisePlanUseCase,
    private val upsertRoutine: UpsertExerciseRoutineUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ExercisePlanState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ExercisePlanEffect>()
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            getPlan().collect { plan ->
                val routines = DayOfWeek.entries.associateWith { day ->
                    plan.forDay(day)?.routine.orEmpty()
                }
                _state.update { it.copy(isLoading = false, routines = routines) }
            }
        }
    }

    fun onIntent(intent: ExercisePlanIntent) {
        when (intent) {
            is ExercisePlanIntent.StartEdit -> _state.update {
                val current = it.routines[intent.day].orEmpty()
                it.copy(editing = EditingExercise(intent.day, current))
            }
            is ExercisePlanIntent.UpdateDraft -> _state.update {
                it.copy(editing = it.editing?.copy(draftRoutine = intent.routine))
            }
            ExercisePlanIntent.SaveRoutine -> saveRoutine()
            ExercisePlanIntent.CancelEdit -> _state.update { it.copy(editing = null) }
        }
    }

    private fun saveRoutine() {
        val editing = _state.value.editing ?: return
        viewModelScope.launch {
            upsertRoutine(ExercisePlanEntry(editing.day, editing.draftRoutine.trim()))
            _state.update { it.copy(editing = null) }
            _effects.emit(ExercisePlanEffect.DismissSheet)
        }
    }
}

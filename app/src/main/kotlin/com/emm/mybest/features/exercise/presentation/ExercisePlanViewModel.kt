package com.emm.mybest.features.exercise.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.models.ExercisePlanEntry
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
import com.emm.mybest.domain.usecase.exercise.UpsertExerciseRoutineUseCase
import com.emm.mybest.features.exercise.presentation.edit.EditingExerciseDraft
import com.emm.mybest.features.exercise.presentation.edit.MAX_DETAIL_LENGTH
import com.emm.mybest.features.exercise.presentation.edit.MAX_NAME_LENGTH
import com.emm.mybest.features.exercise.presentation.edit.MAX_VOLUME_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class ExercisePlanViewModel(
    private val getPlan: GetWeeklyExercisePlanUseCase,
    private val upsertRoutine: UpsertExerciseRoutineUseCase,
    clock: Clock = Clock.System,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ExercisePlanState(today = clock.todayIn(TimeZone.currentSystemDefault())),
    )
    val state = _state.asStateFlow()

    private val _effects = effectFlow<ExercisePlanEffect>()
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            getPlan().collect { plan ->
                val byDay = DayOfWeek.entries.associateWith { day ->
                    plan.forDay(day) ?: ExercisePlanEntry(day)
                }
                _state.update { it.copy(isLoading = false, entries = byDay) }
            }
        }
    }

    fun onIntent(intent: ExercisePlanIntent) {
        when (intent) {
            is ExercisePlanIntent.StartEdit -> handleStartEdit(intent.day)
            is ExercisePlanIntent.UpdateName -> handleUpdateName(intent.name)
            is ExercisePlanIntent.UpdateDetail -> handleUpdateDetail(intent.detail)
            is ExercisePlanIntent.UpdateVolume -> handleUpdateVolume(intent.volume)
            ExercisePlanIntent.SaveRoutine -> saveRoutine()
            ExercisePlanIntent.CancelEdit -> _state.update { it.copy(editing = null) }
        }
    }

    private fun handleStartEdit(day: DayOfWeek) {
        val entry = _state.value.entries[day] ?: ExercisePlanEntry(day)
        val draft = EditingExerciseDraft(
            day = day,
            name = entry.name,
            detail = entry.detail,
            volume = entry.volume,
        )
        _state.update { it.copy(editing = draft) }
    }

    private fun handleUpdateName(name: String) {
        val current = _state.value.editing ?: return
        _state.update { it.copy(editing = current.copy(name = name.take(MAX_NAME_LENGTH))) }
    }

    private fun handleUpdateDetail(detail: String) {
        val current = _state.value.editing ?: return
        _state.update { it.copy(editing = current.copy(detail = detail.take(MAX_DETAIL_LENGTH))) }
    }

    private fun handleUpdateVolume(volume: String) {
        val current = _state.value.editing ?: return
        _state.update { it.copy(editing = current.copy(volume = volume.take(MAX_VOLUME_LENGTH))) }
    }

    private fun saveRoutine() {
        val editing = _state.value.editing ?: return
        viewModelScope.launch {
            upsertRoutine(
                ExercisePlanEntry(
                    dayOfWeek = editing.day,
                    name = editing.name.trim(),
                    detail = editing.detail.trim(),
                    volume = editing.volume.trim(),
                ),
            )
            _state.update { it.copy(editing = null) }
            _effects.emit(ExercisePlanEffect.DismissSheet)
        }
    }
}

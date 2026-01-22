package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddHabitState(
    val ateHealthy: Boolean = false,
    val didExercise: Boolean = false,
    val notes: String = "",
    val isLoading: Boolean = false
)

sealed class AddHabitIntent {
    data class OnAteHealthyChange(val checked: Boolean) : AddHabitIntent()
    data class OnDidExerciseChange(val checked: Boolean) : AddHabitIntent()
    data class OnNotesChange(val notes: String) : AddHabitIntent()
    object OnSaveClick : AddHabitIntent()
}

sealed class AddHabitEffect {
    object NavigateBack : AddHabitEffect()
    data class ShowError(val message: String) : AddHabitEffect()
}

class AddHabitViewModel(
    private val dailyHabitDao: DailyHabitDao
) : ViewModel() {

    private val _state = MutableStateFlow(AddHabitState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddHabitEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: AddHabitIntent) {
        when (intent) {
            is AddHabitIntent.OnAteHealthyChange -> _state.update { it.copy(ateHealthy = intent.checked) }
            is AddHabitIntent.OnDidExerciseChange -> _state.update { it.copy(didExercise = intent.checked) }
            is AddHabitIntent.OnNotesChange -> _state.update { it.copy(notes = intent.notes) }
            AddHabitIntent.OnSaveClick -> saveHabit()
        }
    }

    private fun saveHabit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val entity = DailyHabitEntity(
                    date = LocalDate.now(),
                    ateHealthy = _state.value.ateHealthy,
                    didExercise = _state.value.didExercise,
                    notes = _state.value.notes.takeIf { it.isNotBlank() }
                )
                dailyHabitDao.upsert(entity)
                _effect.emit(AddHabitEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(AddHabitEffect.ShowError("Error al guardar: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

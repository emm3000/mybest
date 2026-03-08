package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.validation.HabitValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.UUID

data class AddHabitState(
    val step: Int = 1,
    // Step 1
    val name: String = "",
    val icon: String = "FitnessCenter",
    val category: String = "Salud",
    // Step 2
    val type: HabitType = HabitType.BOOLEAN,
    val goalValue: String = "",
    val unit: String = "",
    // Step 3
    val scheduledDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),

    val isLoading: Boolean = false,
    val nameError: String? = null,
    val goalError: String? = null
)

sealed class AddHabitIntent {
    // Nav
    object OnNextStep : AddHabitIntent()
    object OnPreviousStep : AddHabitIntent()

    // Step 1
    data class OnNameChange(val name: String) : AddHabitIntent()
    data class OnIconChange(val icon: String) : AddHabitIntent()
    data class OnCategoryChange(val category: String) : AddHabitIntent()

    // Step 2
    data class OnTypeChange(val type: HabitType) : AddHabitIntent()
    data class OnGoalValueChange(val value: String) : AddHabitIntent()
    data class OnUnitChange(val unit: String) : AddHabitIntent()

    // Step 3
    data class OnDayToggle(val day: DayOfWeek) : AddHabitIntent()

    object OnSaveClick : AddHabitIntent()
}

sealed class AddHabitEffect {
    object NavigateBack : AddHabitEffect()
    data class ShowError(val message: String) : AddHabitEffect()
}

class AddHabitViewModel(
    private val createHabitUseCase: com.emm.mybest.domain.usecase.CreateHabitUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddHabitState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddHabitEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: AddHabitIntent) {
        when (intent) {
            AddHabitIntent.OnNextStep -> handleNextStep()
            AddHabitIntent.OnPreviousStep -> _state.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) }
            is AddHabitIntent.OnDayToggle -> toggleDay(intent.day)
            AddHabitIntent.OnSaveClick -> saveHabit()
            else -> handleFormFieldIntent(intent)
        }
    }

    private fun handleFormFieldIntent(intent: AddHabitIntent) {
        when (intent) {
            is AddHabitIntent.OnNameChange -> _state.update { it.copy(name = intent.name, nameError = null) }
            is AddHabitIntent.OnIconChange -> _state.update { it.copy(icon = intent.icon) }
            is AddHabitIntent.OnCategoryChange -> _state.update { it.copy(category = intent.category) }
            is AddHabitIntent.OnTypeChange -> _state.update { it.copy(type = intent.type) }
            is AddHabitIntent.OnGoalValueChange -> _state.update { it.copy(goalValue = intent.value, goalError = null) }
            is AddHabitIntent.OnUnitChange -> _state.update { it.copy(unit = intent.unit) }
            else -> Unit
        }
    }

    private fun handleNextStep() {
        val currentState = _state.value
        when (currentState.step) {
            1 -> {
                val validation = HabitValidator.validateName(currentState.name)
                if (validation.isValid) {
                    _state.update { it.copy(step = 2) }
                } else {
                    _state.update { it.copy(nameError = validation.errorMessage) }
                }
            }
            2 -> {
                val validation = HabitValidator.validateGoal(currentState.type, currentState.goalValue.toFloatOrNull())
                if (validation.isValid) {
                    _state.update { it.copy(step = 3) }
                } else {
                    _state.update { it.copy(goalError = validation.errorMessage) }
                }
            }
        }
    }

    private fun toggleDay(day: DayOfWeek) {
        _state.update {
            val newDays = if (it.scheduledDays.contains(day)) {
                it.scheduledDays - day
            } else {
                it.scheduledDays + day
            }
            it.copy(scheduledDays = newDays)
        }
    }

    private fun saveHabit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val habit = Habit(
                    id = UUID.randomUUID().toString(),
                    name = _state.value.name,
                    icon = _state.value.icon,
                    color = com.emm.mybest.ui.theme.shadcnPrimary.hashCode(), // Using theme color
                    category = _state.value.category,
                    type = _state.value.type,
                    goalValue = _state.value.goalValue.toFloatOrNull(),
                    unit = _state.value.unit,
                    scheduledDays = _state.value.scheduledDays
                )
                createHabitUseCase(habit)
            }.onSuccess {
                _effect.emit(AddHabitEffect.NavigateBack)
            }.onFailure { error ->
                _effect.emit(AddHabitEffect.ShowError("Error al guardar: ${error.message}"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}

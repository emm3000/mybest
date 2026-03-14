package com.emm.mybest.features.habit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitType
import com.emm.mybest.domain.repository.UserPreferencesRepository
import com.emm.mybest.domain.usecase.CreateHabitUseCase
import com.emm.mybest.domain.usecase.GetHabitByIdUseCase
import com.emm.mybest.domain.usecase.UpdateHabitUseCase
import com.emm.mybest.domain.validation.HabitValidator
import com.emm.mybest.ui.theme.shadcnPrimary
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import java.util.UUID

data class AddHabitState(
    val editingHabitId: String? = null,
    val step: Int = 1,
    val name: String = "",
    val icon: String = "FitnessCenter",
    val category: String = "Salud",
    val type: HabitType = HabitType.BOOLEAN,
    val goalValue: String = "",
    val unit: String = "",
    val scheduledDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val reminderUsesDefaultTime: Boolean = true,
    val showTimePicker: Boolean = false,

    val isLoading: Boolean = false,
    val nameError: String? = null,
    val goalError: String? = null,
    val unitError: String? = null,
    val scheduledDaysError: String? = null,
) {
    val isEditMode: Boolean
        get() = editingHabitId != null
}

sealed class AddHabitIntent {
    object OnNextStep : AddHabitIntent()
    object OnPreviousStep : AddHabitIntent()

    data class OnNameChange(val name: String) : AddHabitIntent()
    data class OnIconChange(val icon: String) : AddHabitIntent()
    data class OnCategoryChange(val category: String) : AddHabitIntent()

    data class OnTypeChange(val type: HabitType) : AddHabitIntent()
    data class OnGoalValueChange(val value: String) : AddHabitIntent()
    data class OnUnitChange(val unit: String) : AddHabitIntent()

    data class OnDayToggle(val day: DayOfWeek) : AddHabitIntent()

    object OnTimePickerOpen : AddHabitIntent()
    object OnTimePickerDismiss : AddHabitIntent()
    data class OnReminderEnabledToggle(val enabled: Boolean) : AddHabitIntent()
    data class OnTimePickerConfirm(val hour: Int, val minute: Int) : AddHabitIntent()

    data class LoadHabitForEdit(val habitId: String) : AddHabitIntent()
    object OnSaveClick : AddHabitIntent()
}

sealed class AddHabitEffect {
    object NavigateBack : AddHabitEffect()
    data class ShowError(val message: String) : AddHabitEffect()
}

class AddHabitViewModel(
    private val createHabitUseCase: CreateHabitUseCase,
    private val getHabitByIdUseCase: GetHabitByIdUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddHabitState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddHabitEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            val (h, m) = userPreferencesRepository.defaultReminderTime.first()
            _state.update { current ->
                if (current.editingHabitId != null || !current.reminderUsesDefaultTime) {
                    current
                } else {
                    current.copy(reminderHour = h, reminderMinute = m)
                }
            }
        }
    }

    fun onIntent(intent: AddHabitIntent) {
        if (handleReminderIntent(intent)) return

        when (intent) {
            AddHabitIntent.OnNextStep -> handleNextStep()
            AddHabitIntent.OnPreviousStep -> _state.update { it.copy(step = (it.step - 1).coerceAtLeast(1)) }
            is AddHabitIntent.OnDayToggle -> toggleDay(intent.day)
            is AddHabitIntent.LoadHabitForEdit -> loadHabitForEdit(intent.habitId)
            AddHabitIntent.OnSaveClick -> saveHabit()
            else -> handleFormFieldIntent(intent)
        }
    }

    private fun handleReminderIntent(intent: AddHabitIntent): Boolean {
        when (intent) {
            AddHabitIntent.OnTimePickerOpen -> _state.update { it.copy(showTimePicker = true) }
            AddHabitIntent.OnTimePickerDismiss -> _state.update { it.copy(showTimePicker = false) }
            is AddHabitIntent.OnReminderEnabledToggle -> _state.update { it.copy(reminderEnabled = intent.enabled) }
            is AddHabitIntent.OnTimePickerConfirm -> _state.update {
                it.copy(
                    reminderHour = intent.hour,
                    reminderMinute = intent.minute,
                    reminderUsesDefaultTime = false,
                    showTimePicker = false,
                )
            }
            else -> return false
        }
        return true
    }

    private fun handleFormFieldIntent(intent: AddHabitIntent) {
        when (intent) {
            is AddHabitIntent.OnNameChange -> _state.update { it.copy(name = intent.name, nameError = null) }
            is AddHabitIntent.OnIconChange -> _state.update { it.copy(icon = intent.icon) }
            is AddHabitIntent.OnCategoryChange -> _state.update { it.copy(category = intent.category) }
            is AddHabitIntent.OnTypeChange -> _state.update {
                it.copy(
                    type = intent.type,
                    goalError = null,
                    unitError = null,
                )
            }
            is AddHabitIntent.OnGoalValueChange -> _state.update { it.copy(goalValue = intent.value, goalError = null) }
            is AddHabitIntent.OnUnitChange -> _state.update { it.copy(unit = intent.unit, unitError = null) }
            else -> Unit
        }
    }

    private fun handleNextStep() {
        val currentState: AddHabitState = _state.value
        when (currentState.step) {
            1 -> handleStepOneValidation(currentState)
            2 -> handleStepTwoValidation(currentState)
        }
    }

    private fun handleStepOneValidation(currentState: AddHabitState) {
        val validation = HabitValidator.validateName(currentState.name)
        if (!validation.isValid) {
            _state.update { it.copy(nameError = validation.errorMessage) }
            return
        }
        _state.update { it.copy(step = 2) }
    }

    private fun handleStepTwoValidation(currentState: AddHabitState) {
        val validation = HabitValidator.validateGoal(currentState.type, currentState.goalValue.toFloatOrNull())
        if (!validation.isValid) {
            _state.update { it.copy(goalError = validation.errorMessage, unitError = null) }
            return
        }

        if (currentState.type == HabitType.METRIC && currentState.unit.isBlank()) {
            _state.update { it.copy(unitError = "Ingresa la unidad de medida") }
            return
        }

        _state.update { it.copy(step = 3, goalError = null, unitError = null) }
    }

    private fun toggleDay(day: DayOfWeek) {
        _state.update {
            val newDays = if (it.scheduledDays.contains(day)) {
                it.scheduledDays - day
            } else {
                it.scheduledDays + day
            }
            it.copy(scheduledDays = newDays, scheduledDaysError = null)
        }
    }

    private fun saveHabit() {
        val currentState = _state.value
        if (currentState.scheduledDays.isEmpty()) {
            _state.update {
                it.copy(scheduledDaysError = "Selecciona al menos un día para este hábito")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val habit = Habit(
                    id = currentState.editingHabitId ?: UUID.randomUUID().toString(),
                    name = currentState.name,
                    icon = currentState.icon,
                    color = shadcnPrimary.hashCode(),
                    category = currentState.category,
                    type = currentState.type,
                    goalValue = currentState.goalValue.toFloatOrNull(),
                    unit = currentState.unit,
                    scheduledDays = currentState.scheduledDays,
                    reminderEnabled = currentState.reminderEnabled,
                    reminderHour = if (currentState.reminderUsesDefaultTime) null else currentState.reminderHour,
                    reminderMinute = if (currentState.reminderUsesDefaultTime) null else currentState.reminderMinute,
                )
                saveHabitByMode(habit = habit, isEditMode = currentState.isEditMode)
            }.onSuccess {
                _effect.emit(AddHabitEffect.NavigateBack)
            }.onFailure { error ->
                _effect.emit(AddHabitEffect.ShowError("Error al guardar: ${error.message}"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun saveHabitByMode(habit: Habit, isEditMode: Boolean) {
        if (isEditMode) updateHabitUseCase(habit) else createHabitUseCase(habit)
    }

    private fun loadHabitForEdit(habitId: String) {
        if (_state.value.editingHabitId == habitId) return

        viewModelScope.launch {
            val habit = getHabitByIdUseCase(habitId)
            if (habit == null) {
                _effect.emit(AddHabitEffect.ShowError("No se encontró el hábito para editar"))
                return@launch
            }
            _state.update {
                it.copy(
                    editingHabitId = habit.id,
                    name = habit.name,
                    icon = habit.icon,
                    category = habit.category,
                    type = habit.type,
                    goalValue = habit.goalValue?.toString().orEmpty(),
                    unit = habit.unit.orEmpty(),
                    scheduledDays = habit.scheduledDays,
                    reminderEnabled = habit.reminderEnabled,
                    reminderUsesDefaultTime = habit.reminderHour == null && habit.reminderMinute == null,
                    reminderHour = habit.reminderHour ?: it.reminderHour,
                    reminderMinute = habit.reminderMinute ?: it.reminderMinute,
                    nameError = null,
                    goalError = null,
                    unitError = null,
                    scheduledDaysError = null,
                )
            }
        }
    }
}

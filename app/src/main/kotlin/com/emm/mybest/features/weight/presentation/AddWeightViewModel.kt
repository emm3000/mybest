package com.emm.mybest.features.weight.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.domain.usecase.weight.SaveWeightUseCase
import com.emm.mybest.domain.validation.WeightInputValidator
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddWeightViewModel(
    private val saveWeightUseCase: SaveWeightUseCase,
    private val observeWeightProgressUseCase: ObserveWeightProgressUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddWeightState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddWeightEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            observeWeightProgressUseCase().collect { entries ->
                _state.update { it.copy(lastRecordedWeight = entries.firstOrNull()?.weight) }
            }
        }
    }

    fun onIntent(intent: AddWeightIntent) {
        when (intent) {
            is AddWeightIntent.OnWeightChange -> {
                _state.update {
                    it.copy(
                        weight = intent.weight,
                        weightError = weightErrorFor(intent.weight),
                    )
                }
            }
            is AddWeightIntent.OnNoteChange -> {
                _state.update { it.copy(note = intent.note) }
            }
            AddWeightIntent.OnSaveClick -> saveWeight()
        }
    }

    private fun saveWeight() {
        val currentState = _state.value
        val currentError = weightErrorFor(currentState.weight)
        if (currentError != null) {
            _state.update { it.copy(weightError = currentError) }
            return
        }

        val weightValue = WeightInputValidator.parse(currentState.weight)
        if (weightValue == null) {
            _state.update { it.copy(weightError = INVALID_WEIGHT_MESSAGE) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                saveWeightUseCase(
                    weight = weightValue,
                    note = _state.value.note.takeIf { it.isNotBlank() },
                )
            }.onSuccess {
                _effect.emit(AddWeightEffect.NavigateBack)
            }.onFailure { error ->
                _effect.emit(AddWeightEffect.ShowError("Error al guardar: ${error.message}"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun weightErrorFor(input: String): String? {
        return when {
            input.isBlank() -> null
            !WeightInputValidator.isValidWeightInput(input) -> INVALID_WEIGHT_MESSAGE
            input.endsWith('.') || input.endsWith(',') -> INVALID_WEIGHT_MESSAGE
            else -> null
        }
    }

    companion object {
        private const val INVALID_WEIGHT_MESSAGE = "Ingresa un peso valido. Ejemplo: 72.4 o 72,4"
    }
}

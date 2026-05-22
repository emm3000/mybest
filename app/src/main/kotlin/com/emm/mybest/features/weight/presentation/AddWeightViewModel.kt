package com.emm.mybest.features.weight.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.usecase.weight.ObserveWeightProgressUseCase
import com.emm.mybest.domain.usecase.weight.SaveWeightUseCase
import com.emm.mybest.domain.validation.WeightInputValidator
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

    private val _effect = effectFlow<AddWeightEffect>()
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
        if (input.isBlank()) return null
        val isFormatValid = WeightInputValidator.isValidWeightInput(input) &&
            !input.endsWith('.') && !input.endsWith(',')
        if (!isFormatValid) return INVALID_WEIGHT_MESSAGE
        val parsed = WeightInputValidator.parse(input)
        return when {
            parsed == null -> INVALID_WEIGHT_MESSAGE
            !WeightInputValidator.isWeightInRange(parsed) -> OUT_OF_RANGE_MESSAGE
            else -> null
        }
    }

    companion object {
        private const val INVALID_WEIGHT_MESSAGE = "Ingresa un peso valido. Ejemplo: 72.4 o 72,4"
        private const val OUT_OF_RANGE_MESSAGE = "Peso fuera de rango. Acepta 20–500 kg."
    }
}

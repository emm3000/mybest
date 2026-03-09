package com.emm.mybest.features.weight.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddWeightState(
    val weight: String = "",
    val note: String = "",
    val weightError: String? = null,
    val lastRecordedWeight: Float? = null,
    val isLoading: Boolean = false,
)

sealed class AddWeightIntent {
    data class OnWeightChange(val weight: String) : AddWeightIntent()
    data class OnNoteChange(val note: String) : AddWeightIntent()
    object OnSaveClick : AddWeightIntent()
}

sealed class AddWeightEffect {
    object NavigateBack : AddWeightEffect()
    data class ShowError(val message: String) : AddWeightEffect()
}

class AddWeightViewModel(
    private val weightRepository: WeightRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddWeightState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddWeightEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            weightRepository.getWeightProgress().collect { entries ->
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

        val weightValue = parseWeight(currentState.weight)
        if (weightValue == null) {
            _state.update { it.copy(weightError = INVALID_WEIGHT_MESSAGE) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                weightRepository.saveWeight(
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

    private fun parseWeight(input: String): Float? {
        val normalized = input.replace(',', '.')
        return normalized.toFloatOrNull()
    }

    private fun weightErrorFor(input: String): String? {
        return when {
            input.isBlank() -> null
            !WEIGHT_INPUT_REGEX.matches(input) -> INVALID_WEIGHT_MESSAGE
            input.endsWith('.') || input.endsWith(',') -> INVALID_WEIGHT_MESSAGE
            else -> null
        }
    }

    companion object {
        private val WEIGHT_INPUT_REGEX = Regex("""^\d+([.,]\d{0,2})?$""")
        private const val INVALID_WEIGHT_MESSAGE = "Ingresa un peso valido. Ejemplo: 72.4 o 72,4"
    }
}

package com.emm.mybest.features.weight.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.coroutines.attempt
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.repository.SetupRepository
import com.emm.mybest.domain.usecase.weight.ObserveWeighInTrendUseCase
import com.emm.mybest.domain.usecase.weight.SaveWeightUseCase
import com.emm.mybest.domain.validation.WeightInputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddWeightViewModel(
    private val saveWeightUseCase: SaveWeightUseCase,
    private val observeWeighInTrendUseCase: ObserveWeighInTrendUseCase,
    private val setupRepository: SetupRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AddWeightState())
    val state = _state.asStateFlow()

    private val _effect = effectFlow<AddWeightEffect>()
    val effect = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            observeWeighInTrendUseCase().collect { trend -> _state.update { it.copy(trend = trend) } }
        }
        viewModelScope.launch {
            setupRepository.observe().collect { setup ->
                _state.update { it.withSetup(setup) }
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
            AddWeightIntent.OnOpenSettingsClick -> viewModelScope.launch { _effect.emit(AddWeightEffect.OpenSettings) }
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
            _state.update { it.copy(weightError = AddWeightFieldError.INVALID_FORMAT) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            attempt {
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

    private fun weightErrorFor(input: String): AddWeightFieldError? {
        if (input.isBlank()) return null
        val isFormatValid = WeightInputValidator.isValidWeightInput(input) &&
            !input.endsWith('.') && !input.endsWith(',')
        if (!isFormatValid) return AddWeightFieldError.INVALID_FORMAT
        val parsed = WeightInputValidator.parse(input)
        return when {
            parsed == null -> AddWeightFieldError.INVALID_FORMAT
            !WeightInputValidator.isWeightInRange(parsed) -> AddWeightFieldError.OUT_OF_RANGE
            else -> null
        }
    }
}

package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddWeightState(
    val weight: String = "",
    val note: String = "",
    val isLoading: Boolean = false
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
    private val dailyWeightDao: DailyWeightDao
) : ViewModel() {

    private val _state = MutableStateFlow(AddWeightState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddWeightEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: AddWeightIntent) {
        when (intent) {
            is AddWeightIntent.OnWeightChange -> {
                _state.update { it.copy(weight = intent.weight) }
            }
            is AddWeightIntent.OnNoteChange -> {
                _state.update { it.copy(note = intent.note) }
            }
            AddWeightIntent.OnSaveClick -> saveWeight()
        }
    }

    private fun saveWeight() {
        val weightValue = _state.value.weight.toFloatOrNull()
        if (weightValue == null) {
            viewModelScope.launch { _effect.emit(AddWeightEffect.ShowError("Peso inválido")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val entity = DailyWeightEntity(
                    date = LocalDate.now(),
                    weight = weightValue,
                    note = _state.value.note.takeIf { it.isNotBlank() }
                )
                dailyWeightDao.upsert(entity)
                _effect.emit(AddWeightEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(AddWeightEffect.ShowError("Error al guardar: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

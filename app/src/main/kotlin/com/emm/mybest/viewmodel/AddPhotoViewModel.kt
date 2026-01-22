package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.PhotoType
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddPhotoState(
    val photoUri: String? = null,
    val selectedType: PhotoType = PhotoType.FACE,
    val isLoading: Boolean = false
)

sealed class AddPhotoIntent {
    data class OnPhotoSelected(val uri: String) : AddPhotoIntent()
    data class OnTypeSelected(val type: PhotoType) : AddPhotoIntent()
    object OnSaveClick : AddPhotoIntent()
}

sealed class AddPhotoEffect {
    object NavigateBack : AddPhotoEffect()
    data class ShowError(val message: String) : AddPhotoEffect()
}

class AddPhotoViewModel(
    private val progressPhotoDao: ProgressPhotoDao
) : ViewModel() {

    private val _state = MutableStateFlow(AddPhotoState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddPhotoEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: AddPhotoIntent) {
        when (intent) {
            is AddPhotoIntent.OnPhotoSelected -> _state.update { it.copy(photoUri = intent.uri) }
            is AddPhotoIntent.OnTypeSelected -> _state.update { it.copy(selectedType = intent.type) }
            AddPhotoIntent.OnSaveClick -> savePhoto()
        }
    }

    private fun savePhoto() {
        val uri = _state.value.photoUri
        if (uri == null) {
            viewModelScope.launch { _effect.emit(AddPhotoEffect.ShowError("Debes seleccionar una foto")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val entity = ProgressPhotoEntity(
                    date = LocalDate.now(),
                    type = _state.value.selectedType,
                    photoPath = uri
                )
                progressPhotoDao.insert(entity)
                _effect.emit(AddPhotoEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(AddPhotoEffect.ShowError("Error al guardar: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

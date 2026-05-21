package com.emm.mybest.features.photo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.usecase.photo.SavePhotosUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddPhotoViewModel(
    private val savePhotosUseCase: SavePhotosUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddPhotoState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddPhotoEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: AddPhotoIntent) {
        when (intent) {
            is AddPhotoIntent.OnPhotosSelected -> {
                _state.update { s ->
                    val newPhotos = intent.uris.map { SelectedPhoto(it) }
                    s.copy(selectedPhotos = s.selectedPhotos + newPhotos)
                }
            }
            is AddPhotoIntent.OnTypeSelected -> {
                _state.update { s ->
                    val newList = s.selectedPhotos.toMutableList()
                    if (intent.index in newList.indices) {
                        newList[intent.index] = newList[intent.index].copy(type = intent.type)
                    }
                    s.copy(selectedPhotos = newList)
                }
            }
            is AddPhotoIntent.OnRemovePhoto -> {
                _state.update { s ->
                    val newList = s.selectedPhotos.toMutableList()
                    if (intent.index in newList.indices) {
                        newList.removeAt(intent.index)
                    }
                    s.copy(selectedPhotos = newList)
                }
            }
            AddPhotoIntent.OnSaveClick -> savePhotos()
        }
    }

    private fun savePhotos() {
        val photos = _state.value.selectedPhotos
        if (photos.isEmpty()) {
            viewModelScope.launch { _effect.emit(AddPhotoEffect.ShowError("Debes seleccionar al menos una foto")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching {
                val newPhotos = photos.map { photo ->
                    NewProgressPhoto(
                        type = photo.type,
                        photoPath = photo.uri,
                    )
                }
                savePhotosUseCase(newPhotos)
            }.onSuccess {
                _effect.emit(AddPhotoEffect.NavigateBack)
            }.onFailure { error ->
                _effect.emit(AddPhotoEffect.ShowError("Error al guardar: ${error.message}"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}

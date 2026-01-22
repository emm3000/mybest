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

data class SelectedPhoto(
    val uri: String,
    val type: PhotoType = PhotoType.FACE
)

data class AddPhotoState(
    val selectedPhotos: List<SelectedPhoto> = emptyList(),
    val isLoading: Boolean = false
)

sealed class AddPhotoIntent {
    data class OnPhotosSelected(val uris: List<String>) : AddPhotoIntent()
    data class OnTypeSelected(val index: Int, val type: PhotoType) : AddPhotoIntent()
    data class OnRemovePhoto(val index: Int) : AddPhotoIntent()
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
            try {
                val entities = photos.map { photo ->
                    ProgressPhotoEntity(
                        date = LocalDate.now(),
                        type = photo.type,
                        photoPath = photo.uri
                    )
                }
                progressPhotoDao.insertAll(entities)
                _effect.emit(AddPhotoEffect.NavigateBack)
            } catch (e: Exception) {
                _effect.emit(AddPhotoEffect.ShowError("Error al guardar: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

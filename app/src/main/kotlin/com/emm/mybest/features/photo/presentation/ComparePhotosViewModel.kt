package com.emm.mybest.features.photo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ComparePhotosState(
    val photos: List<ProgressPhoto> = emptyList(),
    val selectedType: PhotoType? = null,
    val beforePhoto: ProgressPhoto? = null,
    val afterPhoto: ProgressPhoto? = null,
    val isLoading: Boolean = false,
)

sealed class ComparePhotosIntent {
    data class OnTypeSelected(val type: PhotoType?) : ComparePhotosIntent()
    data class OnBeforePhotoSelected(val photo: ProgressPhoto) : ComparePhotosIntent()
    data class OnAfterPhotoSelected(val photo: ProgressPhoto) : ComparePhotosIntent()
    object ToggleSwap : ComparePhotosIntent()
}

class ComparePhotosViewModel(
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val _selectedType = MutableStateFlow<PhotoType?>(null)
    private val _beforePhoto = MutableStateFlow<ProgressPhoto?>(null)
    private val _afterPhoto = MutableStateFlow<ProgressPhoto?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ComparePhotosState> = combine(
        _selectedType.flatMapLatest { type ->
            if (type == null) {
                photoRepository.getAllPhotos()
            } else {
                photoRepository.getPhotosByType(type)
            }
        },
        _selectedType,
        _beforePhoto,
        _afterPhoto,
    ) { photos, type, before, after ->
        ComparePhotosState(
            photos = photos,
            selectedType = type,
            beforePhoto = before,
            afterPhoto = after,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ComparePhotosState(isLoading = true),
    )

    fun onIntent(intent: ComparePhotosIntent) {
        when (intent) {
            is ComparePhotosIntent.OnTypeSelected -> {
                _selectedType.value = intent.type
                // Keep selection logic simple while filters change.
                _beforePhoto.value = null
                _afterPhoto.value = null
            }
            is ComparePhotosIntent.OnBeforePhotoSelected -> _beforePhoto.value = intent.photo
            is ComparePhotosIntent.OnAfterPhotoSelected -> _afterPhoto.value = intent.photo
            ComparePhotosIntent.ToggleSwap -> {
                val temp = _beforePhoto.value
                _beforePhoto.value = _afterPhoto.value
                _afterPhoto.value = temp
            }
        }
    }
}

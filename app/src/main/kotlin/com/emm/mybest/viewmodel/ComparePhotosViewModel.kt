package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.PhotoType
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ComparePhotosState(
    val photos: List<ProgressPhotoEntity> = emptyList(),
    val selectedType: PhotoType? = null,
    val beforePhoto: ProgressPhotoEntity? = null,
    val afterPhoto: ProgressPhotoEntity? = null,
    val isLoading: Boolean = false
)

sealed class ComparePhotosIntent {
    data class OnTypeSelected(val type: PhotoType?) : ComparePhotosIntent()
    data class OnBeforePhotoSelected(val photo: ProgressPhotoEntity) : ComparePhotosIntent()
    data class OnAfterPhotoSelected(val photo: ProgressPhotoEntity) : ComparePhotosIntent()
    object ToggleSwap : ComparePhotosIntent()
}

class ComparePhotosViewModel(
    private val progressPhotoDao: ProgressPhotoDao
) : ViewModel() {

    private val _selectedType = MutableStateFlow<PhotoType?>(null)
    private val _beforePhoto = MutableStateFlow<ProgressPhotoEntity?>(null)
    private val _afterPhoto = MutableStateFlow<ProgressPhotoEntity?>(null)

    val state: StateFlow<ComparePhotosState> = combine(
        _selectedType.flatMapLatest { type ->
            if (type == null) progressPhotoDao.observeAll()
            else progressPhotoDao.observeByType(type)
        },
        _selectedType,
        _beforePhoto,
        _afterPhoto
    ) { photos, type, before, after ->
        ComparePhotosState(
            photos = photos,
            selectedType = type,
            beforePhoto = before,
            afterPhoto = after,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ComparePhotosState(isLoading = true)
    )

    fun onIntent(intent: ComparePhotosIntent) {
        when (intent) {
            is ComparePhotosIntent.OnTypeSelected -> {
                _selectedType.value = intent.type
                // Reset selection if they are no longer in the list? 
                // Better to keep them if they exist, but for simplicity let's reset if type changes
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

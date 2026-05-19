package com.emm.mybest.features.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class PhotoViewerState(
    val photos: List<ProgressPhoto> = emptyList(),
    val initialPhotoId: String,
    val isLoading: Boolean = true,
)

class PhotoViewerViewModel(
    private val initialPhotoId: String,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    val state: StateFlow<PhotoViewerState> = photoRepository.getAllPhotos()
        .map { photos ->
            PhotoViewerState(
                photos = photos.sortedByDescending { it.createdAt },
                initialPhotoId = initialPhotoId,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PhotoViewerState(initialPhotoId = initialPhotoId, isLoading = true),
        )
}

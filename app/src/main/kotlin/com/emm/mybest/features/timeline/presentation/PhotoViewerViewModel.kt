package com.emm.mybest.features.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PhotoViewerViewModel(
    private val initialPhotoId: String,
    private val observePhotosUseCase: ObservePhotosUseCase,
) : ViewModel() {

    val state: StateFlow<PhotoViewerState> = observePhotosUseCase()
        .map { photos ->
            PhotoViewerState(
                photos = photos.sortedByDescending { it.createdAt },
                initialPhotoId = initialPhotoId,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = PhotoViewerState(initialPhotoId = initialPhotoId, isLoading = true),
        )
}

package com.emm.mybest.features.photo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComparePhotosViewModel(
    private val beforeId: String,
    private val afterId: String,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val _effect = MutableSharedFlow<ComparePhotosEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect = _effect.asSharedFlow()

    val state: StateFlow<ComparePhotosState> = photoRepository.getAllPhotos()
        .map { photos ->
            ComparePhotosState(
                before = photos.firstOrNull { it.id == beforeId },
                after = photos.firstOrNull { it.id == afterId },
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = ComparePhotosState(isLoading = true),
        )

    fun onIntent(intent: ComparePhotosIntent) {
        when (intent) {
            ComparePhotosIntent.Close -> viewModelScope.launch {
                _effect.emit(ComparePhotosEffect.NavigateBack)
            }
        }
    }
}

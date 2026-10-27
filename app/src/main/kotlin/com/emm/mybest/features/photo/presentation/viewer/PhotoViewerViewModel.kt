package com.emm.mybest.features.photo.presentation.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.usecase.photo.ObservePhotosUseCase
import com.emm.mybest.domain.usecase.photo.ReplacePhotoResult
import com.emm.mybest.domain.usecase.photo.ReplacePhotoUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhotoViewerViewModel(
    private val initialPhotoId: String,
    private val observePhotosUseCase: ObservePhotosUseCase,
    private val replacePhotoUseCase: ReplacePhotoUseCase,
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

    private val _effect = effectFlow<PhotoViewerEffect>()
    val effect: SharedFlow<PhotoViewerEffect> = _effect.asSharedFlow()

    fun onIntent(intent: PhotoViewerIntent) {
        when (intent) {
            is PhotoViewerIntent.OnReplacementCaptured -> replacePhoto(intent)
        }
    }

    private fun replacePhoto(intent: PhotoViewerIntent.OnReplacementCaptured) {
        viewModelScope.launch {
            val result = attempt { replacePhotoUseCase(intent.photoId, intent.uri) }
            _effect.emit(result.fold(::effectOf) { PhotoViewerEffect.ReplaceFailed })
        }
    }

    private fun effectOf(result: ReplacePhotoResult): PhotoViewerEffect = when (result) {
        ReplacePhotoResult.Replaced -> PhotoViewerEffect.PhotoReplaced
        ReplacePhotoResult.PhotoNotFound -> PhotoViewerEffect.PhotoNotFound
    }
}

private suspend inline fun <T> attempt(block: () -> T): Result<T> =
    runCatching { block() }.onFailure { if (it is CancellationException) throw it }

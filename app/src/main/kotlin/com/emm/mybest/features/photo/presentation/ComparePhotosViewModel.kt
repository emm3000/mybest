package com.emm.mybest.features.photo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val MIN_COMPARE_PHOTOS = 2

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

sealed class ComparePhotosEffect {
    data class ShowError(val message: String) : ComparePhotosEffect()
}

class ComparePhotosViewModel(
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val _selectedType = MutableStateFlow<PhotoType?>(null)
    private val _beforePhoto = MutableStateFlow<ProgressPhoto?>(null)
    private val _afterPhoto = MutableStateFlow<ProgressPhoto?>(null)
    private val _effect = MutableSharedFlow<ComparePhotosEffect>()

    val effect = _effect.asSharedFlow()

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
        val resolvedSelection = resolveComparisonSelection(
            photos = photos,
            before = before,
            after = after,
        )
        ComparePhotosState(
            photos = photos,
            selectedType = type,
            beforePhoto = resolvedSelection.before,
            afterPhoto = resolvedSelection.after,
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
            is ComparePhotosIntent.OnBeforePhotoSelected -> {
                if (state.value.afterPhoto?.id == intent.photo.id) {
                    showError("Elige una foto distinta para ANTES.")
                } else {
                    _beforePhoto.value = intent.photo
                }
            }
            is ComparePhotosIntent.OnAfterPhotoSelected -> {
                if (state.value.beforePhoto?.id == intent.photo.id) {
                    showError("Elige una foto distinta para DESPUÉS.")
                } else {
                    _afterPhoto.value = intent.photo
                }
            }
            ComparePhotosIntent.ToggleSwap -> {
                val currentSelection = state.value
                _beforePhoto.value = currentSelection.afterPhoto
                _afterPhoto.value = currentSelection.beforePhoto
            }
        }
    }

    private fun showError(message: String) {
        viewModelScope.launch {
            _effect.emit(ComparePhotosEffect.ShowError(message))
        }
    }
}

private data class ComparisonSelection(
    val before: ProgressPhoto?,
    val after: ProgressPhoto?,
)

private fun resolveComparisonSelection(
    photos: List<ProgressPhoto>,
    before: ProgressPhoto?,
    after: ProgressPhoto?,
): ComparisonSelection {
    val sortedPhotos = photos.sortedBy { it.createdAt }
    val validPhotoIds = sortedPhotos.mapTo(mutableSetOf()) { it.id }

    val resolvedBefore = before?.takeIf { it.id in validPhotoIds } ?: sortedPhotos.firstOrNull()
    val resolvedAfter = after
        ?.takeIf { it.id in validPhotoIds && it.id != resolvedBefore?.id }
        ?: sortedPhotos.lastOrNull { it.id != resolvedBefore?.id }

    if (sortedPhotos.size < MIN_COMPARE_PHOTOS) {
        return ComparisonSelection(
            before = sortedPhotos.firstOrNull(),
            after = null,
        )
    }

    return ComparisonSelection(
        before = resolvedBefore,
        after = resolvedAfter,
    )
}

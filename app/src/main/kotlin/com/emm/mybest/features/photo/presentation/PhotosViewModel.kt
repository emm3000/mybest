package com.emm.mybest.features.photo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.domain.models.NewProgressPhoto
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.photo.GetPhotosOverviewUseCase
import com.emm.mybest.domain.usecase.photo.SavePhotosUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class PhotosViewModel(
    private val getPhotosOverviewUseCase: GetPhotosOverviewUseCase,
    private val savePhotosUseCase: SavePhotosUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val clock: Clock = Clock.System,
) : ViewModel() {

    private val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _selectedType = MutableStateFlow(PhotoType.TRUNK)

    private val _effect = MutableSharedFlow<PhotosEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect = _effect.asSharedFlow()

    val state: StateFlow<PhotosState> = combine(
        getPhotosOverviewUseCase(),
        _selectedType,
    ) { overview, selectedType ->
        val typeOverview = overview.byType[selectedType] ?: emptyPhotoTypeOverview()
        val countByType = overview.byType.mapValues { it.value.count }
        PhotosState(
            selectedType = selectedType,
            overview = typeOverview,
            countByType = countByType,
            isLoading = false,
            today = today,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = PhotosState(isLoading = true, today = today),
    )

    fun onIntent(intent: PhotosIntent) {
        when (intent) {
            is PhotosIntent.SelectType -> _selectedType.update { intent.type }
            is PhotosIntent.OnPhotoCaptured -> savePhoto(intent.uri)
            is PhotosIntent.DeletePhoto -> deletePhoto(intent.id)
            PhotosIntent.OpenCompare -> openCompare()
        }
    }

    private fun savePhoto(uri: String) {
        viewModelScope.launch {
            val type = _selectedType.value
            runCatching {
                savePhotosUseCase(listOf(NewProgressPhoto(photoPath = uri, type = type)))
            }.onFailure { error ->
                _effect.emit(PhotosEffect.ShowError("Error al guardar: ${error.message}"))
            }
        }
    }

    private fun deletePhoto(id: String) {
        viewModelScope.launch {
            runCatching {
                deletePhotoUseCase(id)
            }.onFailure { error ->
                _effect.emit(PhotosEffect.ShowError("Error al eliminar: ${error.message}"))
            }
        }
    }

    private fun openCompare() {
        val currentState = state.value
        val beforeId = currentState.overview.before?.id ?: return
        val afterId = currentState.overview.after?.id ?: return
        viewModelScope.launch {
            _effect.emit(PhotosEffect.NavigateToCompare(beforeId, afterId))
        }
    }
}

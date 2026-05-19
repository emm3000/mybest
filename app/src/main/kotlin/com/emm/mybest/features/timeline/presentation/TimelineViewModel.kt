package com.emm.mybest.features.timeline.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Stable
data class TimelineState(
    val photosByDate: Map<LocalDate, List<ProgressPhoto>> = emptyMap(),
    val photosByMonth: Map<YearMonthValue, List<ProgressPhoto>> = emptyMap(),
    val isLoading: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
)

sealed class TimelineIntent {
    object OnBackClick : TimelineIntent()
    data class EnterSelection(val photoId: String) : TimelineIntent()
    data class ToggleSelection(val photoId: String) : TimelineIntent()
    object ExitSelection : TimelineIntent()
    object DeleteSelected : TimelineIntent()
    object CompareSelected : TimelineIntent()
}

sealed class TimelineEffect {
    object NavigateBack : TimelineEffect()
    object NavigateToCompare : TimelineEffect()
}

private data class SelectionState(
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
)

class TimelineViewModel(
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val _effect = MutableSharedFlow<TimelineEffect>()
    val effect = _effect.asSharedFlow()

    private val _selection = MutableStateFlow(SelectionState())

    private val _photosState = photoRepository.getAllPhotos()
        .map { photos ->
            val sorted = photos.sortedByDescending { it.createdAt }
            Pair(
                photos.groupBy { it.date },
                sorted.groupBy { photo -> YearMonthValue.from(photo.date) },
            )
        }

    val state: StateFlow<TimelineState> = combine(
        _photosState,
        _selection,
    ) { (byDate, byMonth), sel ->
        TimelineState(
            photosByDate = byDate,
            photosByMonth = byMonth,
            isLoading = false,
            selectionMode = sel.selectionMode,
            selectedIds = sel.selectedIds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(FLOW_STOP_TIMEOUT),
        initialValue = TimelineState(isLoading = true),
    )

    fun onIntent(intent: TimelineIntent) {
        viewModelScope.launch {
            when (intent) {
                TimelineIntent.OnBackClick -> _effect.emit(TimelineEffect.NavigateBack)
                is TimelineIntent.EnterSelection -> enterSelection(intent.photoId)
                is TimelineIntent.ToggleSelection -> toggleSelection(intent.photoId)
                TimelineIntent.ExitSelection -> exitSelection()
                TimelineIntent.DeleteSelected -> deleteSelected()
                TimelineIntent.CompareSelected -> _effect.emit(TimelineEffect.NavigateToCompare)
            }
        }
    }

    private fun enterSelection(photoId: String) {
        _selection.value = SelectionState(selectionMode = true, selectedIds = setOf(photoId))
    }

    private fun toggleSelection(photoId: String) {
        val current = _selection.value
        val updated = if (photoId in current.selectedIds) {
            current.selectedIds - photoId
        } else {
            current.selectedIds + photoId
        }
        _selection.value = SelectionState(
            selectionMode = updated.isNotEmpty(),
            selectedIds = updated,
        )
    }

    private fun exitSelection() {
        _selection.value = SelectionState()
    }

    private fun deleteSelected() {
        viewModelScope.launch {
            val ids = _selection.value.selectedIds.toList()
            exitSelection()
            ids.forEach { id -> photoRepository.deletePhoto(id) }
        }
    }

    companion object {
        private const val FLOW_STOP_TIMEOUT = 5000L
    }
}

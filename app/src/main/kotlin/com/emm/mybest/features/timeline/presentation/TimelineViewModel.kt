package com.emm.mybest.features.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TimelineState(
    val photosByDate: Map<LocalDate, List<ProgressPhoto>> = emptyMap(),
    val isLoading: Boolean = false
)

sealed class TimelineIntent {
    object OnBackClick : TimelineIntent()
}

sealed class TimelineEffect {
    object NavigateBack : TimelineEffect()
}

class TimelineViewModel(
    photoRepository: PhotoRepository
) : ViewModel() {

    private val _effect = MutableSharedFlow<TimelineEffect>()
    val effect = _effect.asSharedFlow()

    val state: StateFlow<TimelineState> = photoRepository.getAllPhotos()
        .map { photos ->
            TimelineState(
                photosByDate = photos.groupBy { it.date },
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_STOP_TIMEOUT),
            initialValue = TimelineState(isLoading = true)
        )

    fun onIntent(intent: TimelineIntent) {
        viewModelScope.launch {
            when (intent) {
                TimelineIntent.OnBackClick -> _effect.emit(TimelineEffect.NavigateBack)
            }
        }
    }

    companion object {
        private const val FLOW_STOP_TIMEOUT = 5000L
    }
}

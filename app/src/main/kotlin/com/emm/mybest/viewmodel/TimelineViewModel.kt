package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class TimelineState(
    val photosByDate: Map<LocalDate, List<ProgressPhotoEntity>> = emptyMap(),
    val isLoading: Boolean = false
)

class TimelineViewModel(
    progressPhotoDao: ProgressPhotoDao
) : ViewModel() {

    val state: StateFlow<TimelineState> = progressPhotoDao.observeAll()
        .map { photos ->
            TimelineState(
                photosByDate = photos.groupBy { it.date },
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimelineState(isLoading = true)
        )
}

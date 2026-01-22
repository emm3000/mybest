package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class HistoryState(
    val weights: List<DailyWeightEntity> = emptyList(),
    val habits: List<DailyHabitEntity> = emptyList(),
    val photos: List<ProgressPhotoEntity> = emptyList(),
    val isLoading: Boolean = false
)

class HistoryViewModel(
    private val dailyWeightDao: DailyWeightDao,
    private val dailyHabitDao: DailyHabitDao,
    private val progressPhotoDao: ProgressPhotoDao
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            dailyWeightDao.observeAllOrdered(),
            dailyHabitDao.observeAll(),
            progressPhotoDao.observeAll()
        ) { weights, habits, photos ->
            HistoryState(
                weights = weights.reversed(), // Mostramos los más recientes primero
                habits = habits,
                photos = photos,
                isLoading = false
            )
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }
}

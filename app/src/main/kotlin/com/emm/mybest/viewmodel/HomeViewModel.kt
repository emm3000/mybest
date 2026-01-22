package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.ProgressPhotoDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeState(
    val lastWeight: Float? = null,
    val habitsCompletedToday: Int = 0,
    val totalPhotos: Int = 0,
    val isLoading: Boolean = false
)

sealed class HomeIntent {
    object Refresh : HomeIntent()
}

class HomeViewModel(
    private val dailyWeightDao: DailyWeightDao,
    private val dailyHabitDao: DailyHabitDao,
    private val progressPhotoDao: ProgressPhotoDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Refresh -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            val latestWeight = dailyWeightDao.getLatest()
            val todayHabit = dailyHabitDao.getByDate(LocalDate.now())
            
            val photos = progressPhotoDao.getLastByType(com.emm.mybest.data.entities.PhotoType.FACE)

            _state.value = HomeState(
                lastWeight = latestWeight?.weight,
                habitsCompletedToday = if (todayHabit != null) {
                    (if (todayHabit.ateHealthy) 1 else 0) + (if (todayHabit.didExercise) 1 else 0)
                } else 0,
                totalPhotos = 0,
                isLoading = false
            )
        }
    }
}

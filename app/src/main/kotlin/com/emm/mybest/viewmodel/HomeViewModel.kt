package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.ProgressPhotoDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HomeState(
    val lastWeight: Float? = null,
    val totalWeightLost: Float = 0f,
    val habitsCompletedToday: Int = 0,
    val totalPhotos: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    dailyWeightDao: DailyWeightDao,
    dailyHabitDao: DailyHabitDao,
    progressPhotoDao: ProgressPhotoDao
) : ViewModel() {

    val state: StateFlow<HomeState> = combine(
        dailyWeightDao.observeAllOrdered(),
        dailyHabitDao.observeAll(),
        progressPhotoDao.observeAll()
    ) { weights, habits, photos ->
        val lastWeight = weights.lastOrNull()?.weight
        val firstWeight = weights.firstOrNull()?.weight ?: 0f
        val today = LocalDate.now()
        val todayHabit = habits.find { it.date == today }
        
        HomeState(
            lastWeight = lastWeight,
            totalWeightLost = if (lastWeight != null) firstWeight - lastWeight else 0f,
            habitsCompletedToday = if (todayHabit != null) {
                (if (todayHabit.ateHealthy) 1 else 0) + (if (todayHabit.didExercise) 1 else 0)
            } else 0,
            totalPhotos = photos.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState(isLoading = true)
    )
}

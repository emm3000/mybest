package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.usecase.GetDailyHabitsUseCase
import com.emm.mybest.domain.usecase.ToggleHabitUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeState(
    val dailyHabits: List<HabitWithRecord> = emptyList(),
    val lastWeight: Float? = null,
    val totalWeightLost: Float = 0f,
    val totalPhotos: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    dailyWeightDao: DailyWeightDao,
    progressPhotoDao: ProgressPhotoDao,
    getDailyHabitsUseCase: GetDailyHabitsUseCase,
    private val toggleHabitUseCase: ToggleHabitUseCase
) : ViewModel() {

    val state: StateFlow<HomeState> = combine(
        dailyWeightDao.observeAllOrdered(),
        progressPhotoDao.observeAll(),
        getDailyHabitsUseCase(LocalDate.now())
    ) { weights, photos, dailyHabits ->
        val lastWeight = weights.lastOrNull()?.weight
        val firstWeight = weights.firstOrNull()?.weight ?: 0f

        HomeState(
            dailyHabits = dailyHabits,
            lastWeight = lastWeight,
            totalWeightLost = if (lastWeight != null) firstWeight - lastWeight else 0f,
            totalPhotos = photos.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState(isLoading = true)
    )

    fun onToggleHabit(habitWithRecord: HabitWithRecord) {
        viewModelScope.launch {
            toggleHabitUseCase(habitWithRecord, LocalDate.now())
        }
    }
}

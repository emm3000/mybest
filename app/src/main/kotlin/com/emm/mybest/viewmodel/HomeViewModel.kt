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
import kotlinx.coroutines.flow.asSharedFlow
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

sealed class HomeIntent {
    data class ToggleHabit(val habitWithRecord: HabitWithRecord) : HomeIntent()
    object OnAddWeightClick : HomeIntent()
    object OnAddHabitClick : HomeIntent()
    object OnAddPhotoClick : HomeIntent()
    object OnViewHistoryClick : HomeIntent()
    object OnViewInsightsClick : HomeIntent()
    object OnViewTimelineClick : HomeIntent()
}

sealed class HomeEffect {
    data class ShowError(val message: String) : HomeEffect()
    data class Navigate(val route: com.emm.mybest.navigation.Screen) : HomeEffect()
}

class HomeViewModel(
    dailyWeightDao: DailyWeightDao,
    progressPhotoDao: ProgressPhotoDao,
    getDailyHabitsUseCase: GetDailyHabitsUseCase,
    private val toggleHabitUseCase: ToggleHabitUseCase
) : ViewModel() {

    private val _effect = kotlinx.coroutines.flow.MutableSharedFlow<HomeEffect>()
    val effect = _effect.asSharedFlow()

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

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleHabit -> toggleHabit(intent.habitWithRecord)
            HomeIntent.OnAddWeightClick -> emitNavigate(com.emm.mybest.navigation.Screen.AddWeight)
            HomeIntent.OnAddHabitClick -> emitNavigate(com.emm.mybest.navigation.Screen.AddHabit)
            HomeIntent.OnAddPhotoClick -> emitNavigate(com.emm.mybest.navigation.Screen.AddPhoto)
            HomeIntent.OnViewHistoryClick -> emitNavigate(com.emm.mybest.navigation.Screen.History)
            HomeIntent.OnViewInsightsClick -> emitNavigate(com.emm.mybest.navigation.Screen.Insights)
            HomeIntent.OnViewTimelineClick -> emitNavigate(com.emm.mybest.navigation.Screen.Timeline)
        }
    }

    private fun emitNavigate(screen: com.emm.mybest.navigation.Screen) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.Navigate(screen))
        }
    }

    private fun toggleHabit(habitWithRecord: HabitWithRecord) {
        viewModelScope.launch {
            try {
                toggleHabitUseCase(habitWithRecord, LocalDate.now())
            } catch (e: Exception) {
                // Here we would emit a HomeEffect.ShowError if we had an effect channel
            }
        }
    }
}

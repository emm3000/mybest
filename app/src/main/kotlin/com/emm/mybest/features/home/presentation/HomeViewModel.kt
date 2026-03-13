package com.emm.mybest.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.currentDate
import com.emm.mybest.core.navigation.Screen
import com.emm.mybest.domain.models.HabitWithRecord
import com.emm.mybest.domain.usecase.GetHomeSummaryUseCase
import com.emm.mybest.domain.usecase.ToggleHabitUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeState(
    val dailyHabits: List<HabitWithRecord> = emptyList(),
    val lastWeight: Float? = null,
    val totalWeightLost: Float = 0f,
    val totalPhotos: Int = 0,
    val isLoading: Boolean = false,
) {
    val completedHabitsCount: Int
        get() = dailyHabits.count { it.record?.isCompleted == true }

    val pendingHabitsCount: Int
        get() = (dailyHabits.size - completedHabitsCount).coerceAtLeast(0)
}

sealed class HomeIntent {
    data class ToggleHabit(val habitWithRecord: HabitWithRecord) : HomeIntent()
    data class OnEditHabitClick(val habitId: String) : HomeIntent()
    object OnAddWeightClick : HomeIntent()
    object OnAddHabitClick : HomeIntent()
    object OnAddPhotoClick : HomeIntent()
    object OnReminderSettingsClick : HomeIntent()
    object OnViewHistoryClick : HomeIntent()
    object OnViewInsightsClick : HomeIntent()
    object OnViewTimelineClick : HomeIntent()
}

sealed class HomeEffect {
    data class ShowError(val message: String) : HomeEffect()
    data class ShowSuccess(val message: String) : HomeEffect()
    data class Navigate(val route: Screen) : HomeEffect()
}

class HomeViewModel(
    getHomeSummaryUseCase: GetHomeSummaryUseCase,
    private val toggleHabitUseCase: ToggleHabitUseCase,
) : ViewModel() {

    private val navigationRoutes: Map<HomeIntent, Screen> = mapOf(
        HomeIntent.OnAddWeightClick to Screen.AddWeight,
        HomeIntent.OnAddHabitClick to Screen.AddHabit,
        HomeIntent.OnAddPhotoClick to Screen.AddPhoto,
        HomeIntent.OnReminderSettingsClick to Screen.ReminderSettings,
        HomeIntent.OnViewHistoryClick to Screen.History,
        HomeIntent.OnViewInsightsClick to Screen.Insights,
        HomeIntent.OnViewTimelineClick to Screen.Timeline,
    )

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect = _effect.asSharedFlow()

    val state: StateFlow<HomeState> = getHomeSummaryUseCase()
        .map { summary ->
            HomeState(
                dailyHabits = summary.dailyHabits,
                lastWeight = summary.latestWeight,
                totalWeightLost = summary.totalWeightLost,
                totalPhotos = summary.totalPhotos,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(FLOW_STOP_TIMEOUT),
            initialValue = HomeState(isLoading = true),
        )

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleHabit -> toggleHabit(intent.habitWithRecord)
            is HomeIntent.OnEditHabitClick -> emitNavigate(Screen.EditHabit(intent.habitId))
            else -> handleNavigationIntent(intent)
        }
    }

    private fun handleNavigationIntent(intent: HomeIntent) {
        navigationRoutes[intent]?.let(::emitNavigate)
    }

    private fun emitNavigate(screen: Screen) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.Navigate(screen))
        }
    }

    private fun toggleHabit(habitWithRecord: HabitWithRecord) {
        viewModelScope.launch {
            try {
                toggleHabitUseCase(habitWithRecord, currentDate())
                val successMessage = if (habitWithRecord.record?.isCompleted == true) {
                    "Hábito marcado como pendiente"
                } else {
                    "Hábito completado"
                }
                _effect.emit(HomeEffect.ShowSuccess(successMessage))
            } catch (
                e:
                @Suppress("TooGenericExceptionCaught")
                Exception,
            ) {
                _effect.emit(HomeEffect.ShowError(e.message ?: "Error al actualizar hábito"))
            }
        }
    }

    companion object {
        private const val FLOW_STOP_TIMEOUT = 5000L
    }
}

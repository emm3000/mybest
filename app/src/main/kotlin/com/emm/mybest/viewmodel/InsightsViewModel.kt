package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class InsightsState(
    val weightHistory: List<DailyWeightEntity> = emptyList(),
    val habitConsistency: Float = 0f,
    val totalWeightLost: Float = 0f,
    val currentWeight: Float = 0f,
    val initialWeight: Float = 0f,
    val exerciseDays: Int = 0,
    val healthyEatingDays: Int = 0,
    val isLoading: Boolean = true
)

class InsightsViewModel(
    dailyWeightDao: DailyWeightDao,
    dailyHabitDao: DailyHabitDao
) : ViewModel() {

    val state: StateFlow<InsightsState> = combine(
        dailyWeightDao.observeAllOrdered(),
        dailyHabitDao.observeAll()
    ) { weights, habits ->
        val initialWeight = weights.firstOrNull()?.weight ?: 0f
        val currentWeight = weights.lastOrNull()?.weight ?: 0f
        
        val totalHabits = habits.size * 2 // each day has 2 habits: exercise and healthy eating
        val completedHabits = habits.count { it.didExercise } + habits.count { it.ateHealthy }
        val consistency = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f

        InsightsState(
            weightHistory = weights,
            habitConsistency = consistency,
            totalWeightLost = initialWeight - currentWeight,
            currentWeight = currentWeight,
            initialWeight = initialWeight,
            exerciseDays = habits.count { it.didExercise },
            healthyEatingDays = habits.count { it.ateHealthy },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InsightsState()
    )
}

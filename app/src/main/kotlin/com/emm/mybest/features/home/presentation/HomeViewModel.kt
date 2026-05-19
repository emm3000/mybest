package com.emm.mybest.features.home.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.usecase.compliance.ObserveDailyComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleExerciseComplianceUseCase
import com.emm.mybest.domain.usecase.compliance.ToggleMealComplianceUseCase
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class MealRow(
    val type: MealType,
    val description: String,
    val done: Boolean,
)

@Stable
data class HomeState(
    val isLoading: Boolean = true,
    val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val dayOfWeek: DayOfWeek = Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek,
    val mealRows: List<MealRow> = emptyList(),
    val exerciseRoutine: String = "",
    val exerciseDone: Boolean = false,
    val completionRatio: Float = 0f,
    val completedCount: Int = 0,
    val totalCount: Int = MealType.entries.size + 1,
)

sealed interface HomeIntent {
    data class ToggleMeal(val type: MealType, val done: Boolean) : HomeIntent
    data class ToggleExercise(val done: Boolean) : HomeIntent
}

class HomeViewModel(
    private val observeDailyCompliance: ObserveDailyComplianceUseCase,
    private val toggleMeal: ToggleMealComplianceUseCase,
    private val toggleExercise: ToggleExerciseComplianceUseCase,
    private val getMealPlan: GetWeeklyMealPlanUseCase,
    private val getExercisePlan: GetWeeklyExercisePlanUseCase,
    clock: Clock = Clock.System,
) : ViewModel() {

    private val today: LocalDate = clock.todayIn(TimeZone.currentSystemDefault())
    private val todayDow: DayOfWeek = today.dayOfWeek

    private val _state = MutableStateFlow(HomeState(isLoading = true, today = today, dayOfWeek = todayDow))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        combine(
            getMealPlan(),
            getExercisePlan(),
            observeDailyCompliance(today),
        ) { mealPlan, exPlan, compliance ->
            val rows = MealType.entries.map { type ->
                MealRow(
                    type = type,
                    description = mealPlan.entryFor(todayDow, type)?.description.orEmpty(),
                    done = compliance.mealsDone[type] ?: false,
                )
            }
            val routine = exPlan.forDay(todayDow)?.routine.orEmpty()
            val completedCount = rows.count { it.done } + if (compliance.exerciseDone) 1 else 0
            HomeState(
                isLoading = false,
                today = today,
                dayOfWeek = todayDow,
                mealRows = rows,
                exerciseRoutine = routine,
                exerciseDone = compliance.exerciseDone,
                completionRatio = compliance.completionRatio,
                completedCount = completedCount,
            )
        }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun handle(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleMeal -> viewModelScope.launch {
                toggleMeal(today, intent.type, intent.done)
            }
            is HomeIntent.ToggleExercise -> viewModelScope.launch {
                toggleExercise(today, intent.done)
            }
        }
    }
}

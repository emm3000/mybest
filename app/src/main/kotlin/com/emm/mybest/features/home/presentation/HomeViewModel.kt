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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
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

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val observeDailyCompliance: ObserveDailyComplianceUseCase,
    private val toggleMeal: ToggleMealComplianceUseCase,
    private val toggleExercise: ToggleExerciseComplianceUseCase,
    private val getMealPlan: GetWeeklyMealPlanUseCase,
    private val getExercisePlan: GetWeeklyExercisePlanUseCase,
    clock: Clock = Clock.System,
) : ViewModel() {

    private val dateFlow: Flow<LocalDate> = flow {
        val tz = TimeZone.currentSystemDefault()
        var current = clock.todayIn(tz)
        emit(current)
        while (true) {
            val nowMs = clock.now().toEpochMilliseconds()
            val nextMidnightMs = current.plus(DatePeriod(days = 1))
                .atStartOfDayIn(tz)
                .toEpochMilliseconds()
            delay((nextMidnightMs - nowMs).coerceAtLeast(0L))
            val next = clock.todayIn(tz)
            if (next == current) break
            current = next
            emit(current)
        }
    }

    private val _state = MutableStateFlow(
        HomeState(
            isLoading = true,
            today = clock.todayIn(TimeZone.currentSystemDefault()),
            dayOfWeek = clock.todayIn(TimeZone.currentSystemDefault()).dayOfWeek,
        ),
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val effect = _effect.asSharedFlow()

    init {
        combine(
            getMealPlan(),
            getExercisePlan(),
            dateFlow,
        ) { mealPlan, exPlan, today ->
            Triple(mealPlan, exPlan, today)
        }
            .flatMapLatest { (mealPlan, exPlan, today) ->
                val todayDow = today.dayOfWeek
                observeDailyCompliance(today).map { compliance ->
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
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleMeal -> viewModelScope.launch {
                runCatching {
                    toggleMeal(_state.value.today, intent.type, intent.done)
                }.onFailure { error ->
                    _effect.tryEmit(HomeEffect.ShowError(error.message ?: "Error al actualizar comida"))
                }
            }
            is HomeIntent.ToggleExercise -> viewModelScope.launch {
                runCatching {
                    toggleExercise(_state.value.today, intent.done)
                }.onFailure { error ->
                    _effect.tryEmit(HomeEffect.ShowError(error.message ?: "Error al actualizar ejercicio"))
                }
            }
        }
    }
}

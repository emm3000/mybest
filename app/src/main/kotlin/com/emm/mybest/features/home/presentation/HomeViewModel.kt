package com.emm.mybest.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.DailyCompliance
import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.DailySlotTimes
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.models.WeeklyExercisePlan
import com.emm.mybest.domain.models.WeeklyMealPlan
import com.emm.mybest.domain.usecase.compliance.GetCompletionStreakUseCase
import com.emm.mybest.domain.usecase.compliance.ObserveDailyComplianceUseCase
import com.emm.mybest.domain.usecase.diet.GetWeeklyMealPlanUseCase
import com.emm.mybest.domain.usecase.exercise.GetWeeklyExercisePlanUseCase
import com.emm.mybest.domain.usecase.preferences.ObserveDailySlotTimesUseCase
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
import java.time.temporal.WeekFields
import kotlin.time.Clock

private val PLAN_MEAL_ORDER = listOf(
    MealType.BREAKFAST,
    MealType.LUNCH,
    MealType.SNACK,
    MealType.DINNER,
)

private data class PlanContext(
    val today: LocalDate,
    val todayDow: DayOfWeek,
    val mealPlan: WeeklyMealPlan,
    val exPlan: WeeklyExercisePlan,
    val slotTimes: DailySlotTimes,
)

private fun MealType.toDailySlot(): DailySlot = when (this) {
    MealType.BREAKFAST -> DailySlot.BREAKFAST
    MealType.LUNCH -> DailySlot.LUNCH
    MealType.SNACK -> DailySlot.SNACK
    MealType.DINNER -> DailySlot.DINNER
}

private fun computeWeekNumber(date: LocalDate): Int =
    date.toJavaLocalDate().get(WeekFields.ISO.weekOfWeekBasedYear())

private fun buildMealRows(
    context: PlanContext,
    compliance: DailyCompliance,
): List<PlanRow> = PLAN_MEAL_ORDER.map { type ->
    val slot = type.toDailySlot()
    PlanRow(
        slot = slot,
        time = context.slotTimes[slot],
        description = context.mealPlan.entryFor(context.todayDow, type)?.description.orEmpty(),
        done = compliance.mealsDone[type] ?: false,
    )
}

private fun buildExerciseRow(context: PlanContext, compliance: DailyCompliance): PlanRow =
    PlanRow(
        slot = DailySlot.EXERCISE,
        time = context.slotTimes[DailySlot.EXERCISE],
        description = context.exPlan.forDay(context.todayDow)?.routine.orEmpty(),
        done = compliance.exerciseDone,
    )

private fun buildHomeState(
    compliance: DailyCompliance,
    streak: Int,
    context: PlanContext,
): HomeState {
    val rows = buildMealRows(context, compliance) + buildExerciseRow(context, compliance)
    val completedCount = rows.count { it.done }
    return HomeState(
        isLoading = false,
        today = context.today,
        dayOfWeek = context.todayDow,
        weekNumber = computeWeekNumber(context.today),
        planRows = rows,
        completionRatio = compliance.completionRatio,
        completedCount = completedCount,
        streakDays = streak,
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val observeDailyCompliance: ObserveDailyComplianceUseCase,
    private val toggleUseCases: HomeToggleUseCases,
    private val getMealPlan: GetWeeklyMealPlanUseCase,
    private val getExercisePlan: GetWeeklyExercisePlanUseCase,
    private val getCompletionStreak: GetCompletionStreakUseCase,
    private val observeDailySlotTimes: ObserveDailySlotTimesUseCase,
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
            observeDailySlotTimes(),
        ) { mealPlan, exPlan, today, slotTimes ->
            PlanContext(today, today.dayOfWeek, mealPlan, exPlan, slotTimes)
        }
            .flatMapLatest { context ->
                combine(
                    observeDailyCompliance(context.today),
                    getCompletionStreak(context.today),
                ) { compliance, streak ->
                    buildHomeState(compliance, streak, context)
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleSlot -> handleSlotToggle(intent.slot, intent.done)
        }
    }

    private fun handleSlotToggle(slot: DailySlot, done: Boolean) {
        viewModelScope.launch {
            runCatching {
                when (slot) {
                    DailySlot.BREAKFAST -> toggleUseCases.toggleMeal(_state.value.today, MealType.BREAKFAST, done)
                    DailySlot.LUNCH -> toggleUseCases.toggleMeal(_state.value.today, MealType.LUNCH, done)
                    DailySlot.SNACK -> toggleUseCases.toggleMeal(_state.value.today, MealType.SNACK, done)
                    DailySlot.DINNER -> toggleUseCases.toggleMeal(_state.value.today, MealType.DINNER, done)
                    DailySlot.EXERCISE -> toggleUseCases.toggleExercise(_state.value.today, done)
                }
            }.onFailure { error ->
                _effect.tryEmit(HomeEffect.ShowError(error.message ?: "Error al actualizar"))
            }
        }
    }
}

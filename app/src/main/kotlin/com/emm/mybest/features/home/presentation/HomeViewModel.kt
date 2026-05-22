package com.emm.mybest.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.DailyCompliance
import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.DailySlotTimes
import com.emm.mybest.domain.models.MealPlanEntry
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeeklyExercisePlan
import com.emm.mybest.domain.models.WeeklyMealPlan
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.features.diet.presentation.edit.EditingMealDraft
import com.emm.mybest.features.diet.presentation.edit.toDailySlot
import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.coroutines.flow.update
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

private const val MAX_MEAL_DESCRIPTION_LENGTH = 160

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

private data class HomeComplianceSnapshot(
    val compliance: DailyCompliance,
    val streak: Int,
)

private data class HomeMetricsSnapshot(
    val weights: List<WeightEntry>,
    val photos: List<ProgressPhoto>,
)

private fun DailySlot.toMealType(): MealType? = when (this) {
    DailySlot.BREAKFAST -> MealType.BREAKFAST
    DailySlot.LUNCH -> MealType.LUNCH
    DailySlot.SNACK -> MealType.SNACK
    DailySlot.DINNER -> MealType.DINNER
    DailySlot.EXERCISE -> null
}

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
        description = context.exPlan.forDay(context.todayDow)?.detail.orEmpty(),
        done = compliance.exerciseDone,
    )

private fun buildPlanRows(context: PlanContext, compliance: DailyCompliance): List<PlanRow> =
    buildMealRows(context, compliance) + buildExerciseRow(context, compliance)

private fun buildHomeState(
    complianceSnapshot: HomeComplianceSnapshot,
    context: PlanContext,
    metrics: HomeMetricsSnapshot,
): HomeState = buildPlanRows(context, complianceSnapshot.compliance).let { rows ->
    HomeState(
        isLoading = false,
        today = context.today,
        dayOfWeek = context.todayDow,
        weekNumber = context.today.toJavaLocalDate().get(WeekFields.ISO.weekOfWeekBasedYear()),
        planRows = rows.toImmutableList(),
        completionRatio = complianceSnapshot.compliance.completionRatio,
        completedCount = rows.count { it.done },
        streakDays = complianceSnapshot.streak,
        lastWeightKg = computeLastWeight(metrics.weights),
        previousWeightKg = computePreviousWeight(metrics.weights),
        photoCount = metrics.photos.size,
        lastPhotoType = computeLastPhotoType(metrics.photos),
        lastPhotoDaysAgo = computeLastPhotoDaysAgo(metrics.photos, context.today),
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val useCases: HomeUseCases,
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
            useCases.getMealPlan(),
            useCases.getExercisePlan(),
            dateFlow,
            useCases.observeDailySlotTimes(),
        ) { mealPlan, exPlan, today, slotTimes ->
            PlanContext(today, today.dayOfWeek, mealPlan, exPlan, slotTimes)
        }
            .flatMapLatest { context ->
                combine(
                    useCases.observeDailyCompliance(context.today),
                    useCases.getCompletionStreak(context.today),
                    useCases.observeWeightProgress(),
                    useCases.observePhotos(),
                ) { compliance, streak, weights, photos ->
                    buildHomeState(
                        complianceSnapshot = HomeComplianceSnapshot(compliance, streak),
                        context = context,
                        metrics = HomeMetricsSnapshot(weights, photos),
                    )
                }
            }
            .onEach { newState ->
                _state.update { current -> newState.copy(editingMeal = current.editingMeal) }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleSlot -> handleSlotToggle(intent.slot, intent.done)
            is HomeIntent.StartEditMeal -> handleStartEditMeal(intent.slot)
            is HomeIntent.UpdateMealDraft -> handleUpdateMealDraft(intent.description)
            is HomeIntent.SaveMealDraft -> handleSaveMealDraft()
            is HomeIntent.CancelEditMeal -> handleCancelEditMeal()
        }
    }

    private fun handleSlotToggle(slot: DailySlot, done: Boolean) {
        viewModelScope.launch {
            runCatching {
                when (slot) {
                    DailySlot.BREAKFAST -> useCases.toggleMeal(_state.value.today, MealType.BREAKFAST, done)
                    DailySlot.LUNCH -> useCases.toggleMeal(_state.value.today, MealType.LUNCH, done)
                    DailySlot.SNACK -> useCases.toggleMeal(_state.value.today, MealType.SNACK, done)
                    DailySlot.DINNER -> useCases.toggleMeal(_state.value.today, MealType.DINNER, done)
                    DailySlot.EXERCISE -> useCases.toggleExercise(_state.value.today, done)
                }
            }.onFailure { error ->
                _effect.tryEmit(HomeEffect.ShowError(error.message ?: "Error al actualizar"))
            }
        }
    }

    private fun handleStartEditMeal(slot: DailySlot) {
        val type = slot.toMealType() ?: return
        val row = _state.value.planRows.firstOrNull { it.slot == slot } ?: return
        val draft = EditingMealDraft(
            day = _state.value.dayOfWeek,
            slot = slot,
            type = type,
            description = row.description,
        )
        _state.update { it.copy(editingMeal = draft) }
    }

    private fun handleUpdateMealDraft(description: String) {
        val current = _state.value.editingMeal ?: return
        _state.update {
            it.copy(
                editingMeal = current.copy(description = description.take(MAX_MEAL_DESCRIPTION_LENGTH)),
            )
        }
    }

    private fun handleSaveMealDraft() {
        val draft = _state.value.editingMeal ?: return
        viewModelScope.launch {
            runCatching {
                val entry = MealPlanEntry(draft.day, draft.type, draft.description.trim())
                useCases.upsertMeal(entry)
            }.onFailure { error ->
                _effect.tryEmit(HomeEffect.ShowError(error.message ?: "Error al guardar"))
            }.onSuccess {
                _state.update { it.copy(editingMeal = null) }
            }
        }
    }

    private fun handleCancelEditMeal() {
        _state.update { it.copy(editingMeal = null) }
    }
}

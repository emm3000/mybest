package com.emm.mybest.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn
import java.time.temporal.WeekFields
import kotlin.time.Clock

private fun buildHomeState(
    today: LocalDate,
    weights: List<WeightEntry>,
    photos: List<ProgressPhoto>,
): HomeState = HomeState(
    isLoading = false,
    today = today,
    dayOfWeek = today.dayOfWeek,
    weekNumber = today.toJavaLocalDate().get(WeekFields.ISO.weekOfWeekBasedYear()),
    lastWeightKg = computeLastWeight(weights),
    previousWeightKg = computePreviousWeight(weights),
    photoCount = photos.size,
    lastPhotoType = computeLastPhotoType(photos),
    lastPhotoDaysAgo = computeLastPhotoDaysAgo(photos, today),
)

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

    init {
        combine(
            dateFlow,
            useCases.observeWeightProgress(),
            useCases.observePhotos(),
            ::buildHomeState,
        )
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }
}

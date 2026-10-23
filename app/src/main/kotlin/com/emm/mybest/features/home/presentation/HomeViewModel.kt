package com.emm.mybest.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.domain.models.DailyCheckType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.temporal.WeekFields

private fun buildHomeState(inputs: HomeInputs): HomeState = HomeState(
    isLoading = false,
    today = inputs.today,
    dayOfWeek = inputs.today.dayOfWeek,
    weekNumber = inputs.today.toJavaLocalDate().get(WeekFields.ISO.weekOfWeekBasedYear()),
    lastWeightKg = computeLastWeight(inputs.weights),
    previousWeightKg = computePreviousWeight(inputs.weights),
    photoCount = inputs.photos.size,
    lastPhotoType = computeLastPhotoType(inputs.photos),
    lastPhotoDaysAgo = computeLastPhotoDaysAgo(inputs.photos, inputs.today),
    checks = computeDailyCheckItems(inputs),
)

class HomeViewModel(
    private val useCases: HomeUseCases,
    private val appDayClock: AppDayClock,
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeState(
            isLoading = true,
            today = appDayClock.today(),
            dayOfWeek = appDayClock.today().dayOfWeek,
        ),
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = effectFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        appDayClock.observeToday()
            .flatMapLatest(::observeInputs)
            .onEach { _state.value = buildHomeState(it) }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleDailyCheck -> toggle(intent.type)
            HomeIntent.DefineWeeklyTarget -> _effect.tryEmit(HomeEffect.NavigateToSettings)
        }
    }

    private fun toggle(type: DailyCheckType) {
        viewModelScope.launch { useCases.toggleDailyCheck(_state.value.today, type) }
    }

    private fun observeInputs(today: LocalDate): Flow<HomeInputs> = combine(
        useCases.observeWeightProgress(),
        useCases.observePhotos(),
        useCases.observeDailyChecks(today),
        useCases.observeWeekProgress(today),
    ) { weights, photos, doneToday, weekProgress ->
        HomeInputs(today, weights, photos, doneToday, weekProgress)
    }
}

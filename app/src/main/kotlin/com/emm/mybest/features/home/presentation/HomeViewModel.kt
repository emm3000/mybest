package com.emm.mybest.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.flow.effectFlow
import com.emm.mybest.core.navigation.HomeVisibleDestination
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.usecase.home.HomeDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

private fun buildHomeState(inputs: HomeInputs, destination: HomeDestination): HomeState {
    val checks = computeDailyCheckItems(inputs)
    return HomeState(
        isLoading = false,
        today = inputs.today,
        dayOfWeek = inputs.today.dayOfWeek,
        lastWeightKg = computeLastWeight(inputs.weights),
        previousWeightKg = computePreviousWeight(inputs.weights),
        photoCount = inputs.photos.size,
        lastPhotoType = computeLastPhotoType(inputs.photos),
        lastPhotoDaysAgo = computeLastPhotoDaysAgo(inputs.photos, inputs.today),
        checks = checks,
        keptChecks = computeKeptChecks(inputs.recordedChecks),
        totalChecks = DailyCheckType.entries.size,
        destination = destination,
        shortcuts = computeShortcuts(inputs.agenda.hasPendingCheckIn),
        nextInstallment = computeNextInstallment(inputs.agenda.nextInstallment),
    )
}

class HomeViewModel(
    private val useCases: HomeUseCases,
    private val appDayClock: AppDayClock,
    private val homeVisibleDestination: HomeVisibleDestination,
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

    private val resumes = MutableStateFlow(0)
    private var routedDestination = HomeDestination.DAILY_CHECKS
    private var dismissedDestination: HomeDestination? = null

    init {
        homeVisibleDestination.claim(this)
        appDayClock.observeToday()
            .flatMapLatest(::observeInputs)
            .onEach { _state.value = buildHomeState(it, visibleDestination(it.agenda.destination)) }
            .launchIn(viewModelScope)
        state.filterNot { it.isLoading }
            .map { it.destination }
            .onEach { homeVisibleDestination.publish(this, it) }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        homeVisibleDestination.clear(this)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleDailyCheck -> toggle(intent.type)
            is HomeIntent.AnswerAlcohol -> answerAlcohol(intent.hasDrunk)
            HomeIntent.DefineWeeklyTarget -> _effect.tryEmit(HomeEffect.NavigateToSettings)
            HomeIntent.ShowDailyChecks -> showDailyChecks()
            HomeIntent.Resume -> resumes.update { it + 1 }
        }
    }

    private fun showDailyChecks() {
        dismissedDestination = routedDestination
        _state.update { it.copy(destination = HomeDestination.DAILY_CHECKS) }
    }

    private fun visibleDestination(routed: HomeDestination): HomeDestination {
        routedDestination = routed
        if (routed != dismissedDestination) dismissedDestination = null
        return when {
            dismissedDestination != null -> HomeDestination.DAILY_CHECKS
            isCheckInSettled(routed) -> HomeDestination.CHECK_IN
            else -> routed
        }
    }

    private fun isCheckInSettled(routed: HomeDestination): Boolean =
        routed == HomeDestination.DAILY_CHECKS && _state.value.destination == HomeDestination.CHECK_IN

    private fun toggle(type: DailyCheckType) {
        viewModelScope.launch { useCases.toggleDailyCheck(_state.value.today, type) }
    }

    private fun answerAlcohol(hasDrunk: Boolean) {
        viewModelScope.launch { useCases.recordDailyCheck(_state.value.today, DailyCheckType.ALCOHOL, hasDrunk) }
    }

    private fun observeInputs(today: LocalDate): Flow<HomeInputs> = combine(
        useCases.observeWeightProgress(),
        useCases.observePhotos(),
        useCases.observeDailyChecks(today),
        useCases.observeWeekProgress(today),
        observeAgenda(),
    ) { weights, photos, recordedChecks, weekProgress, agenda ->
        HomeInputs(today, weights, photos, recordedChecks, weekProgress, agenda)
    }

    private fun observeAgenda(): Flow<HomeAgenda> = combine(
        resumes.flatMapLatest { useCases.resolveDestination() },
        useCases.observeHasPendingCheckIn(),
        useCases.observeNextInstallment(),
        ::HomeAgenda,
    )
}

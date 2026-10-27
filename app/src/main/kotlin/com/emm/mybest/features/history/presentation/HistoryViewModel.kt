package com.emm.mybest.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.domain.usecase.history.GetBitacoraUseCase
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

private fun initialHistoryState(today: LocalDate): HistoryState {
    val month = YearMonthValue.from(today)
    return HistoryState(
        today = today,
        currentMonth = month,
        selectedMonth = month,
        monthNavigationLabel = month.toNavigationLabel(),
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val getBitacoraUseCase: GetBitacoraUseCase,
    private val deleteWeightByDate: DeleteWeightByDateUseCase,
    private val deletePhoto: DeletePhotoUseCase,
    appDayClock: AppDayClock,
) : ViewModel() {

    private val baseState = initialHistoryState(appDayClock.today())

    private val _selectedMonth = MutableStateFlow(baseState.currentMonth)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _selectedView = MutableStateFlow(HistoryView.CALENDAR)

    val state: StateFlow<HistoryState> = combine(
        _selectedMonth,
        _selectedDate,
    ) { month, selectedDate ->
        Pair(month, selectedDate)
    }.flatMapLatest { (month, _) ->
        combine(
            getHistoryUseCase(month),
            getBitacoraUseCase(month),
            _selectedDate,
            _selectedView,
        ) { result, bitacora, date, view ->
            baseState.copy(
                selectedMonth = month,
                monthNavigationLabel = month.toNavigationLabel(),
                selectedDate = date,
                monthWeightCount = result.monthWeightCount,
                monthPhotoCount = result.monthPhotoCount,
                monthOutingCount = result.monthOutingCount,
                monthWonNightCount = result.monthWonNightCount,
                dayMarks = result.monthlyData.mapValues { it.value.toMarks() }.toImmutableMap(),
                dayDetail = date?.let { toDayDetailState(it, result.monthlyData[it], baseState.today) },
                recentEntries = result.recentEntries.map { it.toRowState(baseState.today) }.toImmutableList(),
                selectedView = view,
                bitacoraDays = bitacora.map { it.toState(baseState.today) }.toImmutableList(),
                isLoading = false,
                errorMessage = null,
            )
        }
    }.catch { throwable ->
        emit(
            baseState.copy(
                selectedMonth = _selectedMonth.value,
                selectedDate = _selectedDate.value,
                selectedView = _selectedView.value,
                isLoading = false,
                errorMessage = throwable.message ?: "No se pudo cargar el historial.",
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = baseState.copy(isLoading = true),
    )

    fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.OnMonthChange -> _selectedMonth.value = intent.newMonth
            is HistoryIntent.OnDateSelected -> _selectedDate.value = intent.date
            HistoryIntent.OnDateDismiss -> _selectedDate.value = null
            is HistoryIntent.OnViewSelected -> _selectedView.value = intent.view
            is HistoryIntent.OnDeleteWeight -> viewModelScope.launch {
                deleteWeightByDate(intent.date)
            }
            is HistoryIntent.OnDeletePhoto -> viewModelScope.launch {
                deletePhoto(intent.photoId)
            }
        }
    }
}

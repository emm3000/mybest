package com.emm.mybest.features.history.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.domain.usecase.history.GetHistoryUseCase
import com.emm.mybest.domain.usecase.history.HistoryRange
import com.emm.mybest.domain.usecase.history.WeightTrendPoint
import com.emm.mybest.domain.usecase.photo.DeletePhotoUseCase
import com.emm.mybest.domain.usecase.weight.DeleteWeightByDateUseCase
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

@Stable
data class HistoryState(
    val selectedMonth: YearMonthValue = YearMonthValue.now(),
    val selectedRange: HistoryRange = HistoryRange.MONTH,
    val monthlyData: Map<LocalDate, DaySummary> = emptyMap(),
    val weightTrend: List<WeightTrendPoint> = emptyList(),
    val streak: Int = 0,
    val activeDays: Int = 0,
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed class HistoryIntent {
    data class OnMonthChange(val newMonth: YearMonthValue) : HistoryIntent()
    data class OnRangeChange(val range: HistoryRange) : HistoryIntent()
    data class OnDateSelected(val date: LocalDate) : HistoryIntent()
    object OnDateDismiss : HistoryIntent()
    data class OnDeleteWeight(val date: LocalDate) : HistoryIntent()
    data class OnDeletePhoto(val photoId: String) : HistoryIntent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteWeightByDate: DeleteWeightByDateUseCase,
    private val deletePhoto: DeletePhotoUseCase,
    initialMonth: YearMonthValue = YearMonthValue.now(),
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(initialMonth)
    private val _selectedRange = MutableStateFlow(HistoryRange.MONTH)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    val state: StateFlow<HistoryState> = combine(
        _selectedMonth,
        _selectedRange,
        _selectedDate,
    ) { month, range, selectedDate ->
        Triple(month, range, selectedDate)
    }.flatMapLatest { (month, range, selectedDate) ->
        getHistoryUseCase(month, range).combine(_selectedDate) { result, date ->
            HistoryState(
                selectedMonth = month,
                selectedRange = range,
                selectedDate = date,
                monthlyData = result.monthlyData,
                weightTrend = result.weightTrend,
                streak = result.streak,
                activeDays = result.activeDays,
                isLoading = false,
                errorMessage = null,
            )
        }
    }.catch { throwable ->
        emit(
            HistoryState(
                selectedMonth = _selectedMonth.value,
                selectedRange = _selectedRange.value,
                selectedDate = _selectedDate.value,
                isLoading = false,
                errorMessage = throwable.message ?: "No se pudo cargar el historial.",
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = HistoryState(isLoading = true),
    )

    fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.OnMonthChange -> _selectedMonth.value = intent.newMonth
            is HistoryIntent.OnRangeChange -> _selectedRange.value = intent.range
            is HistoryIntent.OnDateSelected -> _selectedDate.value = intent.date
            HistoryIntent.OnDateDismiss -> _selectedDate.value = null
            is HistoryIntent.OnDeleteWeight -> viewModelScope.launch {
                deleteWeightByDate(intent.date)
            }
            is HistoryIntent.OnDeletePhoto -> viewModelScope.launch {
                deletePhoto(intent.photoId)
            }
        }
    }
}

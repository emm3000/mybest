package com.emm.mybest.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.flow.SUBSCRIPTION_TIMEOUT_MS
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

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteWeightByDate: DeleteWeightByDateUseCase,
    private val deletePhoto: DeletePhotoUseCase,
    initialMonth: YearMonthValue = YearMonthValue.now(),
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(initialMonth)
    private val _selectedDate = MutableStateFlow<kotlinx.datetime.LocalDate?>(null)

    val state: StateFlow<HistoryState> = combine(
        _selectedMonth,
        _selectedDate,
    ) { month, selectedDate ->
        Pair(month, selectedDate)
    }.flatMapLatest { (month, _) ->
        getHistoryUseCase(month).combine(_selectedDate) { result, date ->
            HistoryState(
                selectedMonth = month,
                selectedDate = date,
                monthlyData = result.monthlyData.toImmutableMap(),
                monthWeightCount = result.monthWeightCount,
                monthPhotoCount = result.monthPhotoCount,
                recentEntries = result.recentEntries.toImmutableList(),
                isLoading = false,
                errorMessage = null,
            )
        }
    }.catch { throwable ->
        emit(
            HistoryState(
                selectedMonth = _selectedMonth.value,
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

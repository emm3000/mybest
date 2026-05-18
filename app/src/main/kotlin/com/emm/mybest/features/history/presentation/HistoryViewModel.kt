package com.emm.mybest.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class DaySummary(
    val date: LocalDate,
    val weight: WeightEntry? = null,
    val photos: List<ProgressPhoto> = emptyList(),
) {
    val hasWeight: Boolean get() = weight != null
    val hasPhoto: Boolean get() = photos.isNotEmpty()
    val hasActivity: Boolean get() = hasWeight || hasPhoto
}

data class HistoryState(
    val selectedMonth: YearMonthValue = YearMonthValue.now(),
    val monthlyData: Map<LocalDate, DaySummary> = emptyMap(),
    val weekSummary: HistoryWeekSummary = HistoryWeekSummary(),
    val monthSummary: HistoryMonthSummary = HistoryMonthSummary(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed class HistoryIntent {
    data class OnMonthChange(val newMonth: YearMonthValue) : HistoryIntent()
    data class OnDateSelected(val date: LocalDate) : HistoryIntent()
    object OnDateDismiss : HistoryIntent()
    data class OnDeleteWeight(val date: LocalDate) : HistoryIntent()
    data class OnDeletePhoto(val photoId: String) : HistoryIntent()
}

class HistoryViewModel(
    private val weightRepository: WeightRepository,
    private val photoRepository: PhotoRepository,
    initialMonth: YearMonthValue = YearMonthValue.now(),
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(initialMonth)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    val state: StateFlow<HistoryState> = combine(
        _selectedMonth,
        _selectedDate,
        weightRepository.getWeightProgress(),
        photoRepository.getAllPhotos(),
    ) { month, selectedDate, weights, photos ->
        val monthlyData = transformToDaySummary(weights, photos)
        HistoryState(
            selectedMonth = month,
            selectedDate = selectedDate,
            monthlyData = monthlyData,
            weekSummary = buildWeekSummary(month, selectedDate, monthlyData),
            monthSummary = buildMonthSummary(month, monthlyData),
            isLoading = false,
            errorMessage = null,
        )
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
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryState(isLoading = true),
    )

    fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.OnMonthChange -> _selectedMonth.value = intent.newMonth
            is HistoryIntent.OnDateSelected -> _selectedDate.value = intent.date
            HistoryIntent.OnDateDismiss -> _selectedDate.value = null
            is HistoryIntent.OnDeleteWeight -> viewModelScope.launch {
                weightRepository.deleteByDate(intent.date)
            }
            is HistoryIntent.OnDeletePhoto -> viewModelScope.launch {
                photoRepository.deletePhoto(intent.photoId)
            }
        }
    }

    private fun transformToDaySummary(
        weights: List<WeightEntry>,
        photos: List<ProgressPhoto>,
    ): Map<LocalDate, DaySummary> {
        val days = mutableMapOf<LocalDate, DaySummary>()

        weights.forEach { w ->
            val current = days.getOrPut(w.date) { DaySummary(w.date) }
            days[w.date] = current.copy(weight = w)
        }

        photos.forEach { p ->
            val current = days.getOrPut(p.date) { DaySummary(p.date) }
            val currentPhotos = current.photos.toMutableList()
            currentPhotos.add(p)
            days[p.date] = current.copy(photos = currentPhotos)
        }

        return days.toMap()
    }
}

data class HistoryMonthSummary(
    val activityDays: Int = 0,
    val weightDays: Int = 0,
    val photoDays: Int = 0,
)

data class HistoryWeekSummary(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val activityDays: Int = 0,
    val weightDays: Int = 0,
    val photoDays: Int = 0,
)

private fun buildMonthSummary(
    selectedMonth: YearMonthValue,
    monthlyData: Map<LocalDate, DaySummary>,
): HistoryMonthSummary {
    val monthDays = monthlyData.values.filter { YearMonthValue.from(it.date) == selectedMonth }
    return HistoryMonthSummary(
        activityDays = monthDays.count(DaySummary::hasActivity),
        weightDays = monthDays.count(DaySummary::hasWeight),
        photoDays = monthDays.count(DaySummary::hasPhoto),
    )
}

private fun buildWeekSummary(
    selectedMonth: YearMonthValue,
    selectedDate: LocalDate?,
    monthlyData: Map<LocalDate, DaySummary>,
): HistoryWeekSummary {
    val anchorDate = selectedDate ?: monthlyData.keys
        .filter { YearMonthValue.from(it) == selectedMonth }
        .maxOrNull()
        ?: selectedMonth.atDay(1)
    val weekStart = anchorDate.plus(DatePeriod(days = -anchorDate.dayOfWeek.ordinal))
    val weekEnd = weekStart.plus(DatePeriod(days = 6))
    val weekDays = monthlyData.values.filter { summary ->
        summary.date >= weekStart && summary.date <= weekEnd
    }

    return HistoryWeekSummary(
        startDate = weekStart,
        endDate = weekEnd,
        activityDays = weekDays.count(DaySummary::hasActivity),
        weightDays = weekDays.count(DaySummary::hasWeight),
        photoDays = weekDays.count(DaySummary::hasPhoto),
    )
}

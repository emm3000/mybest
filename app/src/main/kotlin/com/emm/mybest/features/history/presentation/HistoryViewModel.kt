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

private const val DAYS_IN_WEEK = 7
private const val MONTHS_IN_YEAR = 12

enum class HistoryRange { WEEK, MONTH, YEAR }

data class WeightTrendPoint(
    val date: LocalDate,
    val weight: Float,
)

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
    val selectedRange: HistoryRange = HistoryRange.MONTH,
    val monthlyData: Map<LocalDate, DaySummary> = emptyMap(),
    val weightTrend: List<WeightTrendPoint> = emptyList(),
    val streak: Int = 0,
    val activeDays: Int = 0,
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // kept for ViewModel tests that still reference monthSummary
    val monthSummary: HistoryMonthSummary = HistoryMonthSummary(),
)

sealed class HistoryIntent {
    data class OnMonthChange(val newMonth: YearMonthValue) : HistoryIntent()
    data class OnRangeChange(val range: HistoryRange) : HistoryIntent()
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
    private val _selectedRange = MutableStateFlow(HistoryRange.MONTH)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    val state: StateFlow<HistoryState> = combine(
        _selectedMonth,
        _selectedRange,
        _selectedDate,
        weightRepository.getWeightProgress(),
        photoRepository.getAllPhotos(),
    ) { month, range, selectedDate, weights, photos ->
        val monthlyData = transformToDaySummary(weights, photos)
        val rangeDates = computeRangeDates(month, range)
        val rangeData = monthlyData.filterKeys { it in rangeDates }
        val activeDays = rangeData.values.count { it.hasActivity }
        val streak = computeStreak(rangeDates, monthlyData)
        val weightTrend = weights
            .filter { it.date in rangeDates }
            .sortedBy { it.date }
            .map { WeightTrendPoint(it.date, it.weight) }

        HistoryState(
            selectedMonth = month,
            selectedRange = range,
            selectedDate = selectedDate,
            monthlyData = monthlyData,
            weightTrend = weightTrend,
            streak = streak,
            activeDays = activeDays,
            monthSummary = buildMonthSummary(month, monthlyData),
            isLoading = false,
            errorMessage = null,
        )
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
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryState(isLoading = true),
    )

    fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.OnMonthChange -> _selectedMonth.value = intent.newMonth
            is HistoryIntent.OnRangeChange -> _selectedRange.value = intent.range
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

internal fun buildMonthSummary(
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

internal fun computeRangeDates(
    anchor: YearMonthValue,
    range: HistoryRange,
): Set<LocalDate> = when (range) {
    HistoryRange.WEEK -> {
        val anchorDay = anchor.atDay(1)
        val weekStart = anchorDay.plus(DatePeriod(days = -anchorDay.dayOfWeek.ordinal))
        (0 until DAYS_IN_WEEK).map { weekStart.plus(DatePeriod(days = it)) }.toSet()
    }
    HistoryRange.MONTH -> {
        val daysInMonth = anchor.lengthOfMonth()
        (1..daysInMonth).map { anchor.atDay(it) }.toSet()
    }
    HistoryRange.YEAR -> {
        val start = YearMonthValue(anchor.year, 1)
        (0 until MONTHS_IN_YEAR).flatMap { monthOffset ->
            val ym = start.plusMonths(monthOffset)
            (1..ym.lengthOfMonth()).map { ym.atDay(it) }
        }.toSet()
    }
}

internal fun computeStreak(
    rangeDates: Set<LocalDate>,
    monthlyData: Map<LocalDate, DaySummary>,
): Int = rangeDates.sorted()
    .fold(intArrayOf(0, 0)) { (max, current), date ->
        val next = if (monthlyData[date]?.hasActivity == true) current + 1 else 0
        intArrayOf(maxOf(max, next), next)
    }[0]

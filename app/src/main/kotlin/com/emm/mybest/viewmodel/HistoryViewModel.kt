package com.emm.mybest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DaySummary(
    val date: LocalDate,
    val weight: DailyWeightEntity? = null,
    val habit: DailyHabitEntity? = null,
    val photos: List<ProgressPhotoEntity> = emptyList()
) {
    val hasWeight: Boolean get() = weight != null
    val hasHabit: Boolean get() = habit != null
    val hasPhoto: Boolean get() = photos.isNotEmpty()
}

data class HistoryState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthlyData: Map<LocalDate, DaySummary> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false
)

sealed class HistoryIntent {
    data class OnMonthChange(val newMonth: YearMonth) : HistoryIntent()
    data class OnDateSelected(val date: LocalDate) : HistoryIntent()
    object OnDateDismiss : HistoryIntent()
    data class OnDeleteWeight(val date: LocalDate) : HistoryIntent()
    data class OnDeleteHabit(val date: LocalDate) : HistoryIntent()
    data class OnDeletePhoto(val photoId: String) : HistoryIntent()
}

class HistoryViewModel(
    private val dailyWeightDao: DailyWeightDao,
    private val dailyHabitDao: DailyHabitDao,
    private val progressPhotoDao: ProgressPhotoDao
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    val state: StateFlow<HistoryState> = combine(
        _selectedMonth,
        _selectedDate,
        dailyWeightDao.observeAllOrdered(),
        dailyHabitDao.observeAll(),
        progressPhotoDao.observeAll()
    ) { month, selectedDate, weights, habits, photos ->
        HistoryState(
            selectedMonth = month,
            selectedDate = selectedDate,
            monthlyData = transformToDaySummary(weights, habits, photos),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryState(isLoading = true)
    )

    fun onIntent(intent: HistoryIntent) {
        when (intent) {
            is HistoryIntent.OnMonthChange -> _selectedMonth.value = intent.newMonth
            is HistoryIntent.OnDateSelected -> _selectedDate.value = intent.date
            HistoryIntent.OnDateDismiss -> _selectedDate.value = null
            is HistoryIntent.OnDeleteWeight -> viewModelScope.launch {
                dailyWeightDao.deleteByDate(intent.date)
            }
            is HistoryIntent.OnDeleteHabit -> viewModelScope.launch {
                dailyHabitDao.deleteByDate(intent.date)
            }
            is HistoryIntent.OnDeletePhoto -> viewModelScope.launch {
                progressPhotoDao.deleteById(intent.photoId)
            }
        }
    }

    private fun transformToDaySummary(
        weights: List<DailyWeightEntity>,
        habits: List<DailyHabitEntity>,
        photos: List<ProgressPhotoEntity>
    ): Map<LocalDate, DaySummary> {
        val days = mutableMapOf<LocalDate, DaySummary>()

        weights.forEach { w ->
            val current = days.getOrPut(w.date) { DaySummary(w.date) }
            days[w.date] = current.copy(weight = w)
        }

        habits.forEach { h ->
            val current = days.getOrPut(h.date) { DaySummary(h.date) }
            days[h.date] = current.copy(habit = h)
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

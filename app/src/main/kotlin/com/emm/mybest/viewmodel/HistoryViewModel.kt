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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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

class HistoryViewModel(
    private val dailyWeightDao: DailyWeightDao,
    private val dailyHabitDao: DailyHabitDao,
    private val progressPhotoDao: ProgressPhotoDao
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        loadMonthData(YearMonth.now())
    }

    fun onMonthChange(newMonth: YearMonth) {
        _state.update { it.copy(selectedMonth = newMonth, isLoading = true) }
        loadMonthData(newMonth)
    }

    fun onDateSelected(date: LocalDate) {
        _state.update { it.copy(selectedDate = date) }
    }
    
    fun onDateDismiss() {
        _state.update { it.copy(selectedDate = null) }
    }

    private fun loadMonthData(month: YearMonth) {
        
        combine(
            dailyWeightDao.observeAllOrdered(),
            dailyHabitDao.observeAll(),
            progressPhotoDao.observeAll()
        ) { weights, habits, photos ->
            
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
            
            days.toMap()
            
        }.onEach { fullMap ->
            _state.update { it.copy(monthlyData = fullMap, isLoading = false) }
        }.launchIn(viewModelScope)
    }
}

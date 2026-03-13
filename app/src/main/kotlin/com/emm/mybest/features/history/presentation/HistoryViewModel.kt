package com.emm.mybest.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.DailyHabitSummary
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.DailyHabitRepository
import com.emm.mybest.domain.repository.HabitRepository
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class DaySummary(
    val date: LocalDate,
    val weight: WeightEntry? = null,
    val weightHabitName: String? = null,
    val habit: DailyHabitSummary? = null,
    val photos: List<ProgressPhoto> = emptyList(),
    val photoHabitNames: Map<String, String> = emptyMap(),
) {
    val hasWeight: Boolean get() = weight != null
    val hasHabit: Boolean get() = habit != null
    val hasPhoto: Boolean get() = photos.isNotEmpty()
    val hasActivity: Boolean get() = hasWeight || hasHabit || hasPhoto
}

data class HistoryState(
    val selectedMonth: YearMonthValue = YearMonthValue.now(),
    val monthlyData: Map<LocalDate, DaySummary> = emptyMap(),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed class HistoryIntent {
    data class OnMonthChange(val newMonth: YearMonthValue) : HistoryIntent()
    data class OnDateSelected(val date: LocalDate) : HistoryIntent()
    object OnDateDismiss : HistoryIntent()
    data class OnDeleteWeight(val date: LocalDate) : HistoryIntent()
    data class OnDeleteHabit(val date: LocalDate) : HistoryIntent()
    data class OnDeletePhoto(val photoId: String) : HistoryIntent()
}

class HistoryViewModel(
    private val weightRepository: WeightRepository,
    private val dailyHabitRepository: DailyHabitRepository,
    private val photoRepository: PhotoRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonthValue.now())
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    private val baseData = combine(
        _selectedMonth,
        _selectedDate,
        weightRepository.getWeightProgress(),
        dailyHabitRepository.getAllDailyHabits(),
        photoRepository.getAllPhotos(),
    ) { month, selectedDate, weights, habits, photos ->
        BaseHistoryData(
            month = month,
            selectedDate = selectedDate,
            weights = weights,
            habits = habits,
            photos = photos,
        )
    }

    val state: StateFlow<HistoryState> = combine(
        baseData,
        habitRepository.getAllHabits(),
    ) { base, allHabits ->
        val habitNameById = allHabits.associate { it.id to it.name }
        HistoryState(
            selectedMonth = base.month,
            selectedDate = base.selectedDate,
            monthlyData = transformToDaySummary(base.weights, base.habits, base.photos, habitNameById),
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
            is HistoryIntent.OnDeleteHabit -> viewModelScope.launch {
                dailyHabitRepository.deleteByDate(intent.date)
            }
            is HistoryIntent.OnDeletePhoto -> viewModelScope.launch {
                photoRepository.deletePhoto(intent.photoId)
            }
        }
    }

    private fun transformToDaySummary(
        weights: List<WeightEntry>,
        habits: List<DailyHabitSummary>,
        photos: List<ProgressPhoto>,
        habitNameById: Map<String, String>,
    ): Map<LocalDate, DaySummary> {
        val days = mutableMapOf<LocalDate, DaySummary>()

        weights.forEach { w ->
            val current = days.getOrPut(w.date) { DaySummary(w.date) }
            days[w.date] = current.copy(
                weight = w,
                weightHabitName = w.habitId?.let(habitNameById::get),
            )
        }

        habits.forEach { h ->
            val current = days.getOrPut(h.date) { DaySummary(h.date) }
            days[h.date] = current.copy(habit = h)
        }

        photos.forEach { p ->
            val current = days.getOrPut(p.date) { DaySummary(p.date) }
            val currentPhotos = current.photos.toMutableList()
            currentPhotos.add(p)
            val currentPhotoHabitNames = current.photoHabitNames.toMutableMap()
            val habitName = p.habitId?.let(habitNameById::get)
            if (habitName != null) {
                currentPhotoHabitNames[p.id] = habitName
            }
            days[p.date] = current.copy(
                photos = currentPhotos,
                photoHabitNames = currentPhotoHabitNames,
            )
        }

        return days.toMap()
    }
}

private data class BaseHistoryData(
    val month: YearMonthValue,
    val selectedDate: LocalDate?,
    val weights: List<WeightEntry>,
    val habits: List<DailyHabitSummary>,
    val photos: List<ProgressPhoto>,
)

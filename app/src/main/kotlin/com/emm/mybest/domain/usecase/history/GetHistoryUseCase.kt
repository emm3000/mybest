package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val DAYS_IN_WEEK = 7
private const val MONTHS_IN_YEAR = 12

data class DaySummary(
    val date: LocalDate,
    val weight: WeightEntry? = null,
    val photos: List<ProgressPhoto> = emptyList(),
) {
    val hasWeight: Boolean get() = weight != null
    val hasPhoto: Boolean get() = photos.isNotEmpty()
    val hasActivity: Boolean get() = hasWeight || hasPhoto
}

data class WeightTrendPoint(
    val date: LocalDate,
    val weight: Float,
)

enum class HistoryRange { WEEK, MONTH, YEAR }

data class HistoryResult(
    val monthlyData: Map<LocalDate, DaySummary>,
    val weightTrend: List<WeightTrendPoint>,
    val streak: Int,
    val activeDays: Int,
)

class GetHistoryUseCase(
    private val weightRepository: WeightRepository,
    private val photoRepository: PhotoRepository,
) {
    operator fun invoke(
        month: YearMonthValue,
        range: HistoryRange,
    ): Flow<HistoryResult> = combine(
        weightRepository.getWeightProgress(),
        photoRepository.getAllPhotos(),
    ) { weights, photos ->
        val monthlyData = buildDaySummaryMap(weights, photos)
        val rangeDates = computeRangeDates(month, range)
        val rangeData = monthlyData.filterKeys { it in rangeDates }
        val activeDays = rangeData.values.count { it.hasActivity }
        val streak = computeStreak(rangeDates, monthlyData)
        val weightTrend = weights
            .filter { it.date in rangeDates }
            .sortedBy { it.date }
            .map { WeightTrendPoint(it.date, it.weight) }

        HistoryResult(
            monthlyData = monthlyData,
            weightTrend = weightTrend,
            streak = streak,
            activeDays = activeDays,
        )
    }
}

fun buildDaySummaryMap(
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

fun computeRangeDates(
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

fun computeStreak(
    rangeDates: Set<LocalDate>,
    monthlyData: Map<LocalDate, DaySummary>,
): Int = rangeDates.sorted()
    .fold(intArrayOf(0, 0)) { (max, current), date ->
        val next = if (monthlyData[date]?.hasActivity == true) current + 1 else 0
        intArrayOf(maxOf(max, next), next)
    }[0]

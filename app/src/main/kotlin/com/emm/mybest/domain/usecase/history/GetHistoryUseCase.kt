package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class GetHistoryUseCase(
    private val weightRepository: WeightRepository,
    private val photoRepository: PhotoRepository,
) {
    operator fun invoke(month: YearMonthValue): Flow<HistoryResult> = combine(
        weightRepository.getWeightProgress(),
        photoRepository.getAllPhotos(),
    ) { weights, photos ->
        val monthlyData = buildDaySummaryMap(weights, photos)
        val monthDates = buildMonthDates(month)
        val monthWeightCount = weights.count { it.date in monthDates }
        val monthPhotoCount = photos.count { it.date in monthDates }
        val recentEntries = buildRecentEntries(monthDates, monthlyData)

        HistoryResult(
            monthlyData = monthlyData,
            monthWeightCount = monthWeightCount,
            monthPhotoCount = monthPhotoCount,
            recentEntries = recentEntries,
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

private fun buildMonthDates(month: YearMonthValue): Set<LocalDate> {
    val daysInMonth = month.lengthOfMonth()
    return (1..daysInMonth).map { month.atDay(it) }.toSet()
}

private fun buildRecentEntries(
    monthDates: Set<LocalDate>,
    monthlyData: Map<LocalDate, DaySummary>,
): List<HistoryRecentEntry> = monthDates
    .mapNotNull { date ->
        val summary = monthlyData[date] ?: return@mapNotNull null
        if (!summary.hasActivity) return@mapNotNull null
        HistoryRecentEntry(
            date = date,
            weight = summary.weight?.weight,
            photoTypes = summary.photos.map { it.type }.sortedBy(PhotoType::ordinal),
        )
    }
    .sortedByDescending { it.date }

package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.DailyCheckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

private data class BodyRecords(
    val weights: List<WeightEntry>,
    val photos: List<ProgressPhoto>,
)

private data class NightRecords(
    val outings: List<Outing>,
    val wonNights: List<WonNight>,
    val reasonsByDay: Map<LocalDate, Set<RelapseReason>>,
)

private data class MonthRecords(
    val body: BodyRecords,
    val nights: NightRecords,
    val checks: List<DailyCheck>,
)

class GetHistoryUseCase(
    private val body: HistoryBodySources,
    private val nights: HistoryNightSources,
    private val dailyCheckRepository: DailyCheckRepository,
) {

    /**
     * Emits what was recorded on each app day of [month], with the relapse reasons of its outings. Weigh-ins and
     * photos are keyed by their own date; outings, won nights and daily checks by their app day.
     */
    operator fun invoke(month: YearMonthValue): Flow<HistoryResult> {
        val days = month.days()
        return combine(
            bodyRecords(),
            nightRecords(days),
            dailyCheckRepository.observeRange(days.start, days.endInclusive),
            ::MonthRecords,
        ).map { it.toResult(days) }
    }

    private fun bodyRecords(): Flow<BodyRecords> = combine(
        body.weightRepository.getWeightProgress(),
        body.photoRepository.getAllPhotos(),
        ::BodyRecords,
    )

    private fun nightRecords(days: ClosedRange<LocalDate>): Flow<NightRecords> = combine(
        nights.outingRepository.observeRange(days.start, days.endInclusive),
        nights.outingRepository.observeWonNights(days.start, days.endInclusive),
        nights.observeRelapses().map { relapses -> relapses.associate { it.outing.appDay to it.reasons } },
        ::NightRecords,
    )
}

private fun MonthRecords.toResult(days: ClosedRange<LocalDate>): HistoryResult {
    val summaries = toSummaries()
    return HistoryResult(
        monthlyData = summaries,
        monthWeightCount = body.weights.count { it.date in days },
        monthPhotoCount = body.photos.count { it.date in days },
        monthOutingCount = nights.outings.size,
        monthWonNightCount = nights.wonNights.size,
        recentEntries = buildRecentEntries(days, summaries),
    )
}

private fun MonthRecords.toSummaries(): Map<LocalDate, DaySummary> {
    val weightsByDay = body.weights.associateBy { it.date }
    val photosByDay = body.photos.groupBy { it.date }
    val outingsByDay = nights.outings.associateBy { it.appDay }
    val wonNightsByDay = nights.wonNights.associateBy { it.appDay }
    val checksByDay = checks.groupBy { it.appDay }
    val dates = weightsByDay.keys + photosByDay.keys + outingsByDay.keys + wonNightsByDay.keys + checksByDay.keys
    return dates.associateWith { date ->
        DaySummary(
            date = date,
            weight = weightsByDay[date],
            photos = photosByDay[date].orEmpty(),
            outing = outingsByDay[date],
            relapseReasons = nights.reasonsByDay[date].orEmpty(),
            wonNight = wonNightsByDay[date],
            checks = checksByDay[date].orEmpty(),
        )
    }
}

private fun buildRecentEntries(
    days: ClosedRange<LocalDate>,
    summaries: Map<LocalDate, DaySummary>,
): List<HistoryRecentEntry> = summaries.values
    .filter { it.date in days && (it.hasWeight || it.hasPhoto) }
    .sortedByDescending { it.date }
    .map { summary ->
        HistoryRecentEntry(
            date = summary.date,
            weight = summary.weight?.weight,
            photoTypes = summary.photos.map { it.type }.sortedBy(PhotoType::ordinal),
        )
    }

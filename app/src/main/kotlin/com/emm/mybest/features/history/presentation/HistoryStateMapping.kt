package com.emm.mybest.features.history.presentation

import com.emm.mybest.core.datetime.MONTH_ABBR_ES
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.LimitResult
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.usecase.history.DaySummary
import com.emm.mybest.ui.components.toOutcome
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import java.util.Locale

internal fun YearMonthValue.toNavigationLabel(): String {
    val previous = MONTH_ABBR_ES.getValue(minusMonths(1).month)
    val next = MONTH_ABBR_ES.getValue(plusMonths(1).month)
    return "← $previous · $next →"
}

internal fun DaySummary.toMarks(): DayMarksState = DayMarksState(
    checkCount = doneCheckCount,
    marks = listOfNotNull(
        outingMark(),
        HistoryMarkKind.WON_NIGHT.takeIf { hasWonNight },
        HistoryMarkKind.WEIGHT.takeIf { hasWeight },
        HistoryMarkKind.PHOTO.takeIf { hasPhoto },
    ).toImmutableList(),
)

internal fun toDayDetailState(date: LocalDate, summary: DaySummary?, today: LocalDate): DayDetailState {
    if (summary == null || !summary.hasActivity) return DayDetailState(date = date)
    val canDelete = date == today
    return DayDetailState(
        date = date,
        outing = summary.outing?.toDetail(summary.relapseReasons),
        wonNight = summary.wonNight?.toDetail(),
        checks = summary.toCheckDetails(),
        weight = summary.weight?.let { WeightRowState(it.toLabel(), canDelete) },
        photos = summary.photos.map { PhotoRowState(it, canDelete, it.type.toDescriptionRes()) }.toImmutableList(),
        isEmpty = false,
    )
}

private fun DaySummary.outingMark(): HistoryMarkKind? = when {
    isRelapse -> HistoryMarkKind.RELAPSE
    hasOuting -> HistoryMarkKind.OUTING
    else -> null
}

private fun Outing.toDetail(reasons: Set<RelapseReason>): OutingDetailState = OutingDetailState(
    kind = kind,
    isPendingCheckIn = isPendingCheckIn,
    spentLabel = checkIn?.totalSpent?.formatPen(),
    mood = checkIn?.mood,
    outcomes = limitResults().map(LimitResult::toOutcome).toImmutableList(),
    relapseReasons = reasons.sortedBy(RelapseReason::ordinal).toImmutableList(),
)

private fun WonNight.toDetail(): WonNightDetailState =
    estimatedCost?.let { WonNightDetailState.Estimated(it.formatPen()) } ?: WonNightDetailState.Unestimated

private fun DaySummary.toCheckDetails() = DailyCheckType.entries
    .map { type -> CheckDetailState(type, answerFor(type)) }
    .toImmutableList()

private fun DaySummary.answerFor(type: DailyCheckType): CheckAnswer {
    val check = checks.firstOrNull { it.type == type } ?: return CheckAnswer.UNRECORDED
    return if (check.isDone) CheckAnswer.YES else CheckAnswer.NO
}

internal fun WeightEntry.toLabel(): String {
    val base = String.format(Locale.ROOT, "%.1f kg", weight)
    return if (note.isNullOrBlank()) base else "$base · $note"
}

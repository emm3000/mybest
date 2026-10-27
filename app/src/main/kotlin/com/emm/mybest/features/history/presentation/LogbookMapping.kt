package com.emm.mybest.features.history.presentation

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.core.datetime.formatHhMm
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.usecase.history.LogbookDay
import com.emm.mybest.domain.usecase.history.LogbookEntry
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

internal fun LogbookDay.toState(today: LocalDate): LogbookDayState = LogbookDayState(
    appDay = appDay,
    dateLabel = formatRecentDate(appDay),
    isToday = appDay == today,
    rows = entries.map(LogbookEntry::toRow).toImmutableList(),
)

internal fun List<LogbookDayState>.noteTextOf(noteId: Long): String? =
    asSequence()
        .flatMap { it.rows }
        .firstOrNull { it.noteId == noteId }
        ?.parts
        ?.filterIsInstance<LogbookTextPart.Text>()
        ?.singleOrNull()
        ?.value

internal fun textRes(@StringRes resId: Int, vararg args: Any): LogbookTextPart =
    LogbookTextPart.Res(resId, args.toList())

@Suppress("CyclomaticComplexMethod")
private fun LogbookEntry.toRow(): LogbookRowState = when (this) {
    is LogbookEntry.OutingEntry -> toOutingRow()
    is LogbookEntry.WonNightEntry -> row(R.string.logbook_kind_won_night, wonNight.toParts())
    LogbookEntry.StayedInEntry -> row(R.string.logbook_kind_stayed_in, listOf(textRes(R.string.logbook_stayed_in)))
    is LogbookEntry.ReturnEntry -> row(
        R.string.logbook_kind_return,
        listOf(LogbookTextPart.Plural(R.plurals.logbook_return_days, days)),
    )
    is LogbookEntry.WeighInEntry -> row(
        R.string.logbook_kind_weigh_in,
        listOf(LogbookTextPart.Text(weight.toLabel())),
    )
    is LogbookEntry.PhotosEntry -> row(R.string.logbook_kind_photo, types.toParts())
    is LogbookEntry.InstallmentEntry -> row(
        R.string.logbook_kind_installment,
        listOf(LogbookTextPart.Text(debtName), LogbookTextPart.Text(amount.formatPen())),
    )
    is LogbookEntry.ContributionEntry -> row(
        R.string.logbook_kind_contribution,
        listOf(textRes(R.string.logbook_contribution, amount.formatPen())),
    )
    is LogbookEntry.NoteEntry -> LogbookRowState(
        label = LogbookTextPart.Text(time.formatHhMm()),
        parts = listOf(LogbookTextPart.Text(note.text)),
        noteId = note.id,
    )
}

private fun row(@StringRes labelRes: Int, parts: List<LogbookTextPart>): LogbookRowState =
    LogbookRowState(textRes(labelRes), parts)

private fun WonNight.toParts(): List<LogbookTextPart> {
    val estimate = estimatedCost?.let { textRes(R.string.logbook_won_night_estimate, it.formatPen()) }
        ?: textRes(R.string.history_detail_won_night_no_estimate)
    return listOf(textRes(R.string.logbook_won_night_stayed), estimate)
}

private fun List<PhotoType>.toParts(): List<LogbookTextPart> =
    groupingBy { it }.eachCount().map { (type, count) -> LogbookTextPart.Plural(type.toPluralRes(), count) }

@PluralsRes
private fun PhotoType.toPluralRes(): Int = when (this) {
    PhotoType.TRUNK -> R.plurals.logbook_photos_trunk
    PhotoType.FACE -> R.plurals.logbook_photos_face
}

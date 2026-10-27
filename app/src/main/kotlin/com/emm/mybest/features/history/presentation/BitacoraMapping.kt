package com.emm.mybest.features.history.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.usecase.history.BitacoraDay
import com.emm.mybest.domain.usecase.history.BitacoraEntry
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

internal fun BitacoraDay.toState(today: LocalDate): BitacoraDayState = BitacoraDayState(
    appDay = appDay,
    dateLabel = formatRecentDate(appDay),
    isToday = appDay == today,
    rows = entries.map(BitacoraEntry::toRow).toImmutableList(),
)

internal fun textRes(@StringRes resId: Int, vararg args: Any): BitacoraTextPart =
    BitacoraTextPart.Res(resId, args.toList())

@Suppress("CyclomaticComplexMethod")
private fun BitacoraEntry.toRow(): BitacoraRowState = when (this) {
    is BitacoraEntry.OutingEntry -> toOutingRow()
    is BitacoraEntry.WonNightEntry -> row(R.string.bitacora_kind_won_night, wonNight.toParts())
    BitacoraEntry.StayedInEntry -> row(R.string.bitacora_kind_stayed_in, listOf(textRes(R.string.bitacora_stayed_in)))
    is BitacoraEntry.ReturnEntry -> row(
        R.string.bitacora_kind_return,
        listOf(BitacoraTextPart.Plural(R.plurals.bitacora_return_days, days)),
    )
    is BitacoraEntry.WeighInEntry -> row(
        R.string.bitacora_kind_weigh_in,
        listOf(BitacoraTextPart.Text(weight.toLabel())),
    )
    is BitacoraEntry.PhotosEntry -> row(R.string.bitacora_kind_photo, types.map { textRes(it.toLabelRes()) })
    is BitacoraEntry.InstallmentEntry -> row(
        R.string.bitacora_kind_installment,
        listOf(BitacoraTextPart.Text(debtName), BitacoraTextPart.Text(amount.formatPen())),
    )
    is BitacoraEntry.ContributionEntry -> row(
        R.string.bitacora_kind_contribution,
        listOf(textRes(R.string.bitacora_contribution, amount.formatPen())),
    )
}

private fun row(@StringRes labelRes: Int, parts: List<BitacoraTextPart>): BitacoraRowState =
    BitacoraRowState(labelRes, parts)

private fun WonNight.toParts(): List<BitacoraTextPart> {
    val estimate = estimatedCost?.let { textRes(R.string.bitacora_won_night_estimate, it.formatPen()) }
        ?: textRes(R.string.history_detail_won_night_no_estimate)
    return listOf(textRes(R.string.bitacora_won_night_stayed), estimate)
}

@StringRes
private fun PhotoType.toLabelRes(): Int = when (this) {
    PhotoType.TRUNK -> R.string.photo_type_trunk
    PhotoType.FACE -> R.string.photo_type_face
}

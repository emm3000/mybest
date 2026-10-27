package com.emm.mybest.features.history.presentation

import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.usecase.history.BitacoraEntry

internal fun BitacoraEntry.OutingEntry.toOutingRow(): BitacoraRowState {
    val isRelapse = relapseReasons.isNotEmpty()
    val labelRes = if (isRelapse) R.string.bitacora_kind_relapse else R.string.bitacora_kind_outing
    return BitacoraRowState(textRes(labelRes), outingParts(), isRelapse)
}

private fun BitacoraEntry.OutingEntry.outingParts(): List<BitacoraTextPart> =
    listOf(textRes(outing.kind.labelRes())) + spendParts() + limitsKeptParts() + reasonParts() + moodParts()

private fun BitacoraEntry.OutingEntry.spendParts(): List<BitacoraTextPart> {
    val checkIn = outing.checkIn ?: return listOf(textRes(R.string.history_detail_check_in_pending))
    return listOf(textRes(R.string.bitacora_spent, checkIn.totalSpent.formatPen()))
}

private fun BitacoraEntry.OutingEntry.limitsKeptParts(): List<BitacoraTextPart> {
    val isKept = relapseReasons.isEmpty() && outing.kind == OutingKind.PLANNED && outing.checkIn != null
    return if (isKept) listOf(textRes(R.string.bitacora_limits_kept)) else emptyList()
}

private fun BitacoraEntry.OutingEntry.reasonParts(): List<BitacoraTextPart> = relapseReasons
    .filter { it != RelapseReason.UNPLANNED }
    .sortedBy(RelapseReason::ordinal)
    .map { textRes(it.labelRes()) }

private fun BitacoraEntry.OutingEntry.moodParts(): List<BitacoraTextPart> =
    outing.checkIn?.let { listOf(textRes(R.string.bitacora_mood, it.mood)) }.orEmpty()

package com.emm.mybest.features.history.presentation

import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.usecase.history.LogbookEntry

internal fun LogbookEntry.OutingEntry.toOutingRow(): LogbookRowState {
    val isRelapse = relapseReasons.isNotEmpty()
    val labelRes = if (isRelapse) R.string.logbook_kind_relapse else R.string.logbook_kind_outing
    return LogbookRowState(textRes(labelRes), outingParts(), isRelapse)
}

private fun LogbookEntry.OutingEntry.outingParts(): List<LogbookTextPart> =
    listOf(textRes(outing.kind.labelRes())) + spendParts() + limitsKeptParts() + reasonParts() + moodParts()

private fun LogbookEntry.OutingEntry.spendParts(): List<LogbookTextPart> {
    val checkIn = outing.checkIn ?: return listOf(textRes(R.string.history_detail_check_in_pending))
    return listOf(textRes(R.string.logbook_spent, checkIn.totalSpent.formatPen()))
}

private fun LogbookEntry.OutingEntry.limitsKeptParts(): List<LogbookTextPart> {
    val isKept = relapseReasons.isEmpty() && outing.kind == OutingKind.PLANNED && outing.checkIn != null
    return if (isKept) listOf(textRes(R.string.logbook_limits_kept)) else emptyList()
}

private fun LogbookEntry.OutingEntry.reasonParts(): List<LogbookTextPart> = relapseReasons
    .filter { it != RelapseReason.UNPLANNED }
    .sortedBy(RelapseReason::ordinal)
    .map { textRes(it.labelRes()) }

private fun LogbookEntry.OutingEntry.moodParts(): List<LogbookTextPart> =
    outing.checkIn?.let { listOf(textRes(R.string.logbook_mood, it.mood)) }.orEmpty()

package com.emm.mybest.features.summary.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.core.datetime.MONTH_ABBR_ES
import com.emm.mybest.core.datetime.shortEs
import com.emm.mybest.domain.models.ReturnTime
import com.emm.mybest.domain.models.WeekRecovery
import com.emm.mybest.ui.theme.AtelierInk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

private const val SINGULAR_DAYS = 1

private data class DaysTexts(
    @StringRes val oneRes: Int,
    @StringRes val otherRes: Int,
)

private val RETURNED_TEXTS = DaysTexts(R.string.summary_recovery_returned_one, R.string.summary_recovery_returned_other)
private val OPEN_TEXTS = DaysTexts(R.string.summary_recovery_open_one, R.string.summary_recovery_open_other)
private val HISTORY_TEXTS = DaysTexts(R.string.summary_days_one, R.string.summary_days_other)

internal fun WeekRecovery.toRecoverySection(today: LocalDate): SummarySectionState = SummarySectionState(
    titleRes = R.string.summary_section_recovery,
    rows = listOf(latestReturnTime.toHeadlineRow(today)) + returnTimeHistory.map { it.toHistoryRow() },
)

private fun ReturnTime?.toHeadlineRow(today: LocalDate): SummaryRowState = SummaryRowState.Line(
    label = SummaryText(R.string.summary_recovery_label),
    value = this?.toHeadlineText(today) ?: SummaryText(R.string.summary_recovery_none),
    valueColor = AtelierInk,
)

private fun ReturnTime.toHeadlineText(today: LocalDate): SummaryText = when (days) {
    null -> daysText(relapseAppDay.daysUntil(today), OPEN_TEXTS)
    else -> daysText(days, RETURNED_TEXTS)
}

private fun ReturnTime.toHistoryRow(): SummaryRowState = SummaryRowState.Line(
    label = SummaryText(R.string.summary_recovery_history_label, listOf(relapseAppDay.toDayLabel())),
    value = days?.let { daysText(it, HISTORY_TEXTS) }
        ?: SummaryText(R.string.summary_recovery_history_open),
    valueColor = AtelierInk,
)

private fun daysText(days: Int, texts: DaysTexts): SummaryText =
    SummaryText(if (days == SINGULAR_DAYS) texts.oneRes else texts.otherRes, listOf(days))

private fun LocalDate.toDayLabel(): String = "${dayOfWeek.shortEs()} $day ${MONTH_ABBR_ES.getValue(month.ordinal + 1)}"

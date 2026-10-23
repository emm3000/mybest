package com.emm.mybest.features.summary.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.emm.mybest.R
import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.LimitsKept
import com.emm.mybest.domain.models.WeekOutings
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk

internal fun WeekOutings.toOutingsSection(): SummarySectionState = SummarySectionState(
    titleRes = R.string.summary_section_outings,
    rows = listOf(
        plainLine(R.string.summary_outings_count, count.toString()),
        plainLine(R.string.summary_outings_spent, totalSpent.formatPen()),
        plainLine(R.string.summary_outings_pending, pendingCheckIns.toString()),
    ),
)

internal fun LimitsKept.toLimitsSection(): SummarySectionState = SummarySectionState(
    titleRes = R.string.summary_section_limits,
    rows = listOf(
        SummaryRowState.Line(
            label = SummaryText(R.string.summary_limits_label),
            value = toValueText(),
            valueColor = if (isAllKept()) AtelierDone else AtelierInk,
        ),
    ),
)

internal fun plainLine(@StringRes labelRes: Int, value: String, valueColor: Color = AtelierInk): SummaryRowState =
    SummaryRowState.Line(SummaryText(labelRes), SummaryText.literal(value), valueColor)

private fun LimitsKept.isAllKept(): Boolean = checkedIn > 0 && kept == checkedIn

private fun LimitsKept.toValueText(): SummaryText = when (checkedIn) {
    0 -> SummaryText(R.string.summary_limits_none)
    else -> SummaryText(R.string.summary_limits_kept, listOf(kept, checkedIn))
}

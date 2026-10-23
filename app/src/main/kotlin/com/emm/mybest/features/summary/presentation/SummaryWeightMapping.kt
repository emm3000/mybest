package com.emm.mybest.features.summary.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.domain.models.WeighInDirection
import com.emm.mybest.domain.models.WeighInTrend
import com.emm.mybest.ui.theme.AtelierInk
import kotlin.math.abs

internal fun WeighInTrend.toWeightSection(): SummarySectionState = SummarySectionState(
    titleRes = R.string.summary_section_weight,
    rows = latestKg?.let { toRows(it) } ?: listOf(SummaryRowState.SetupPrompt(SummaryText(R.string.summary_weight_setup))),
)

private fun WeighInTrend.toRows(latest: Float): List<SummaryRowState> = listOfNotNull(
    weightLine(R.string.summary_weight_latest, kgText(R.string.summary_weight_value, latest)),
    changeSincePreviousKg?.let { weightLine(R.string.summary_weight_change, changeText(it)) },
    remainingToTargetKg?.let {
        weightLine(
            R.string.summary_weight_remaining,
            kgText(R.string.summary_weight_value, it),
        )
    },
)

private fun WeighInTrend.changeText(change: Float): SummaryText = when (direction) {
    WeighInDirection.DOWN -> kgText(R.string.summary_weight_change_down, abs(change))
    WeighInDirection.UP -> kgText(R.string.summary_weight_change_up, change)
    else -> kgText(R.string.summary_weight_value, change)
}

private fun kgText(@StringRes res: Int, kg: Float): SummaryText = SummaryText(res, listOf(kg))

private fun weightLine(@StringRes labelRes: Int, value: SummaryText): SummaryRowState =
    SummaryRowState.Line(SummaryText(labelRes), value, AtelierInk)

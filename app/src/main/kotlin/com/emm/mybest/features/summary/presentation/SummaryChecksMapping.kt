package com.emm.mybest.features.summary.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.WeekProgress
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierInk

internal fun Map<DailyCheckType, WeekProgress>.toChecksSection(): SummarySectionState = SummarySectionState(
    titleRes = R.string.summary_section_checks,
    rows = DailyCheckType.entries.mapNotNull { type -> get(type)?.toRow(type) }.ifEmpty { listOf(CHECKS_SETUP_PROMPT) },
)

private val CHECKS_SETUP_PROMPT = SummaryRowState.SetupPrompt(
    SummaryText(R.string.summary_checks_setup),
    SummaryDestination.SETTINGS,
)

private fun WeekProgress.toRow(type: DailyCheckType): SummaryRowState = SummaryRowState.Line(
    label = SummaryText(type.toLabelRes()),
    value = toValueText(type),
    valueColor = if (isOnTrack(type)) AtelierDone else AtelierInk,
)

private fun WeekProgress.toValueText(type: DailyCheckType): SummaryText = when (type) {
    DailyCheckType.ALCOHOL -> SummaryText(R.string.summary_checks_maximum, listOf(count, target))
    else -> SummaryText(R.string.summary_checks_progress, listOf(count, target))
}

private fun WeekProgress.isOnTrack(type: DailyCheckType): Boolean = when (type) {
    DailyCheckType.ALCOHOL -> count <= target
    else -> count >= target
}

@StringRes
private fun DailyCheckType.toLabelRes(): Int = when (this) {
    DailyCheckType.GYM -> R.string.summary_check_gym
    DailyCheckType.DIET -> R.string.summary_check_diet
    DailyCheckType.ENGLISH -> R.string.summary_check_english
    DailyCheckType.ALCOHOL -> R.string.summary_check_alcohol
}

package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.ui.components.LimitOutcomeState
import com.emm.mybest.ui.components.message

@Composable
internal fun HistoryOutingDetail(outing: OutingDetailState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HistoryDetailLine(
            DetailLineState(
                label = stringResource(R.string.history_detail_outing_label),
                value = stringResource(outing.kind.labelRes()),
            ),
        )
        if (outing.isPendingCheckIn) PendingCheckInLine()
        outing.spentLabel?.let { SpentLine(spentLabel = it) }
        outing.outcomes.forEach { OutcomeLine(outcome = it) }
        outing.mood?.let { MoodLine(mood = it) }
        outing.relapseReasons.forEach { ReasonLine(reason = it) }
    }
}

@Composable
private fun PendingCheckInLine() {
    HistoryDetailLine(
        DetailLineState(
            label = stringResource(R.string.history_detail_spent_label),
            value = stringResource(R.string.history_detail_check_in_pending),
        ),
    )
}

@Composable
private fun SpentLine(spentLabel: String) {
    HistoryDetailLine(DetailLineState(label = stringResource(R.string.history_detail_spent_label), value = spentLabel))
}

@Composable
private fun MoodLine(mood: Int) {
    HistoryDetailLine(
        DetailLineState(
            label = stringResource(R.string.history_detail_mood_label),
            value = stringResource(R.string.history_detail_mood_value, mood),
        ),
    )
}

@Composable
private fun OutcomeLine(outcome: LimitOutcomeState) {
    HistoryDetailLine(
        DetailLineState(
            label = stringResource(outcome.kind.labelRes()),
            value = outcome.message(),
            tone = outcome.tone(),
        ),
    )
}

@Composable
private fun ReasonLine(reason: RelapseReason) {
    HistoryDetailLine(
        DetailLineState(
            label = stringResource(R.string.history_detail_relapse_label),
            value = stringResource(reason.labelRes()),
            tone = DetailLineTone.ALERT,
        ),
    )
}

private fun LimitOutcomeState.tone(): DetailLineTone = when (this) {
    is LimitOutcomeState.Kept -> DetailLineTone.KEPT
    else -> DetailLineTone.ALERT
}

package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R
import com.emm.mybest.ui.components.checkLabelRes
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun HistoryWonNightDetail(wonNight: WonNightDetailState, modifier: Modifier = Modifier) {
    HistoryDetailLine(
        line = DetailLineState(
            label = stringResource(R.string.history_detail_won_night_label),
            value = wonNight.message(),
        ),
        modifier = modifier,
    )
}

@Composable
internal fun HistoryChecksDetail(checks: ImmutableList<CheckDetailState>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        checks.forEach { check ->
            HistoryDetailLine(
                DetailLineState(
                    label = stringResource(checkLabelRes(check.type)),
                    value = stringResource(checkAnswerRes(check.answer)),
                ),
            )
        }
    }
}

@Composable
private fun WonNightDetailState.message(): String = when (this) {
    is WonNightDetailState.Estimated -> stringResource(R.string.history_detail_won_night_estimate, amountLabel)
    WonNightDetailState.Unestimated -> stringResource(R.string.history_detail_won_night_no_estimate)
}

package com.emm.mybest.features.preouting.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton

private val BUTTON_MIN_HEIGHT = 56.dp
private val BUTTON_GAP = 12.dp

@Composable
internal fun PreOutingDecisionArea(
    state: PreOutingState,
    onIntent: (PreOutingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val decision = state.decision) {
        PreOutingDecisionState.Undecided -> DecisionButtons(state.canStayIn, onIntent, modifier)
        is PreOutingDecisionState.WentOut -> WentOutSummary(decision, modifier)
        is PreOutingDecisionState.StayedIn -> StayedInSummary(decision, modifier)
    }
}

@Composable
private fun DecisionButtons(
    canStayIn: Boolean,
    onIntent: (PreOutingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BUTTON_GAP)) {
        if (canStayIn) {
            HButton(
                onClick = { onIntent(PreOutingIntent.OnStayInClick) },
                modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
            ) {
                Text(text = stringResource(R.string.pre_outing_action_stay_in))
            }
        }
        HButton(
            onClick = { onIntent(PreOutingIntent.OnGoOutClick) },
            modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
            variant = ButtonVariant.Outline,
        ) {
            Text(text = stringResource(R.string.pre_outing_action_go_out))
        }
    }
}

@Composable
private fun WentOutSummary(decision: PreOutingDecisionState.WentOut, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.pre_outing_decision_went_out),
            style = MaterialTheme.typography.headlineSmall,
        )
        decision.moneyLabel?.let { ValueRow(stringResource(R.string.pre_outing_decision_money), it) }
        decision.drinksLabel?.let { ValueRow(stringResource(R.string.pre_outing_decision_drinks), it) }
        decision.returnTimeLabel?.let { ValueRow(stringResource(R.string.pre_outing_decision_return_time), it) }
    }
}

@Composable
private fun StayedInSummary(decision: PreOutingDecisionState.StayedIn, modifier: Modifier = Modifier) {
    val confirmation = decision.estimateLabel
        ?.let { stringResource(R.string.pre_outing_won_night_added, it) }
        ?: stringResource(R.string.pre_outing_won_night_added_unknown)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BUTTON_GAP)) {
        Text(
            text = stringResource(R.string.pre_outing_decision_stayed_in),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = confirmation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

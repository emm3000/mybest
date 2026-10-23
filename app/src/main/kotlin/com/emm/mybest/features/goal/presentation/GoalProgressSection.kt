package com.emm.mybest.features.goal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierDone
import com.emm.mybest.ui.theme.AtelierHairline
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierTheme

private val SECTION_GAP = 10.dp
private val BAR_HEIGHT = 4.dp

@Composable
internal fun GoalProgressSection(
    goal: GoalSummaryState,
    onIntent: (PurchaseGoalIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SECTION_GAP)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            MicroLabel(text = stringResource(R.string.purchase_goal_label, goal.nameLabel))
            Text(
                text = stringResource(R.string.purchase_goal_percent, goal.percent),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        ProgressBar(fraction = goal.progressFraction)
        GoalAmounts(goal = goal)
        HButton(
            text = stringResource(R.string.purchase_goal_add_contribution),
            onClick = { onIntent(PurchaseGoalIntent.OnAddContributionClick) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(BAR_HEIGHT).background(AtelierHairline)) {
        Box(modifier = Modifier.fillMaxWidth(fraction).height(BAR_HEIGHT).background(AtelierDone))
    }
}

@Composable
private fun GoalAmounts(goal: GoalSummaryState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SECTION_GAP / 2)) {
        Text(
            text = stringResource(R.string.purchase_goal_price, goal.priceLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = AtelierInkSecondary,
        )
        Text(
            text = stringResource(R.string.purchase_goal_contributed, goal.contributedLabel),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.purchase_goal_left, goal.leftLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = AtelierInkSecondary,
        )
    }
}

private val PREVIEW_GOAL =
    GoalSummaryState("Zapatillas", "ZAPATILLAS", "S/ 450.00", "S/ 180.00", "S/ 270.00", 40, 0.4f, emptyList())

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun GoalProgressSectionPreview() {
    AtelierTheme {
        GoalProgressSection(goal = PREVIEW_GOAL, onIntent = {})
    }
}

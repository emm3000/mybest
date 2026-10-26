package com.emm.mybest.features.goal.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierTheme

private val CONTENT_GUTTER = 24.dp
private val SECTION_GAP = 28.dp
private val ACTION_VERTICAL = 10.dp
private val ACTION_GAP = 24.dp

@Composable
fun PurchaseGoalContent(
    state: PurchaseGoalState,
    onIntent: (PurchaseGoalIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(CONTENT_GUTTER),
        verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
    ) {
        if (state.isLoaded) {
            val goal = state.goal
            if (goal == null || state.goalForm.isEditing) {
                DefineGoalSection(form = state.goalForm, onIntent = onIntent)
            } else {
                GoalProgressSection(goal = goal, onIntent = onIntent)
            }
            NotSpentSection(amountLabel = state.notSpentLabel)
            state.goal?.let {
                ContributionsAndActions(goal = it, isEditing = state.goalForm.isEditing, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun ContributionsAndActions(
    goal: GoalSummaryState,
    isEditing: Boolean,
    onIntent: (PurchaseGoalIntent) -> Unit,
) {
    ContributionList(contributions = goal.contributions, onIntent = onIntent)
    Row(horizontalArrangement = Arrangement.spacedBy(ACTION_GAP)) {
        if (!isEditing) {
            GoalAction(
                textRes = R.string.purchase_goal_edit,
                intent = PurchaseGoalIntent.OnEditClick,
                onIntent = onIntent,
            )
        }
        GoalAction(
            textRes = R.string.purchase_goal_clear,
            intent = PurchaseGoalIntent.OnClearClick,
            onIntent = onIntent,
        )
    }
}

@Composable
private fun GoalAction(
    @StringRes textRes: Int,
    intent: PurchaseGoalIntent,
    onIntent: (PurchaseGoalIntent) -> Unit,
) {
    MicroLabel(
        text = stringResource(textRes),
        modifier = Modifier
            .clickable { onIntent(intent) }
            .padding(vertical = ACTION_VERTICAL),
    )
}

private val PREVIEW_GOAL = GoalSummaryState(
    name = "Zapatillas",
    nameLabel = "ZAPATILLAS",
    priceLabel = "S/ 450.00",
    contributedLabel = "S/ 180.00",
    leftLabel = "S/ 270.00",
    percent = 40,
    progressFraction = 0.4f,
    contributions = listOf(
        ContributionItemState(2, "Viernes 23 octubre", "S/ 80.00"),
        ContributionItemState(1, "Viernes 9 octubre", "S/ 100.00"),
    ),
    priceInput = "450",
)

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun PurchaseGoalContentPreview() {
    AtelierTheme {
        PurchaseGoalContent(
            state = PurchaseGoalState(isLoaded = true, goal = PREVIEW_GOAL, notSpentLabel = "S/ 380.00"),
            onIntent = {},
        )
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun PurchaseGoalContentEmptyPreview() {
    AtelierTheme {
        PurchaseGoalContent(state = PurchaseGoalState(isLoaded = true, notSpentLabel = "S/ 0.00"), onIntent = {})
    }
}

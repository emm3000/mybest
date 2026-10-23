package com.emm.mybest.features.preouting.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.theme.AtelierTheme

private val CONTENT_GUTTER = 24.dp
private val SECTION_GAP = 32.dp
private val HERO_GAP = 4.dp

@Composable
fun PreOutingContent(
    state: PreOutingState,
    onIntent: (PreOutingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val onPromptClick: (PreOutingPrompt) -> Unit = { onIntent(PreOutingIntent.OnPromptClick(it)) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CONTENT_GUTTER, vertical = SECTION_GAP),
        verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
    ) {
        PreOutingHero(state = state)
        Column {
            ValueRow(stringResource(R.string.pre_outing_spent_label), state.spentOnOutingsLabel)
            InstallmentLines(state = state, onPromptClick = onPromptClick)
            GoalLines(state = state, onPromptClick = onPromptClick)
            OutingLines(state = state, onPromptClick = onPromptClick)
            ValueRow(stringResource(R.string.pre_outing_not_spent_label), state.notSpentLabel)
        }
        PreOutingDecisionArea(state = state, onIntent = onIntent)
    }
}

@Composable
private fun PreOutingHero(state: PreOutingState) {
    Column(verticalArrangement = Arrangement.spacedBy(HERO_GAP)) {
        Text(text = stringResource(R.string.pre_outing_headline), style = MaterialTheme.typography.headlineLarge)
        if (state.estimatedOutingCostLabel != null) {
            Text(text = state.estimatedOutingCostLabel, style = MaterialTheme.typography.displaySmall)
            Text(
                text = stringResource(state.estimateCaptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InstallmentLines(state: PreOutingState, onPromptClick: (PreOutingPrompt) -> Unit) {
    if (state.nightPercent != null) {
        val text = stringResource(
            R.string.pre_outing_installment_share,
            state.nightPercent,
            state.debtName,
            state.paymentDay,
        )
        SentenceRow(text = text)
    }
    PromptLines(PreOutingPrompt.TYPICAL_COST, state, onPromptClick)
    PromptLines(PreOutingPrompt.DEBTS, state, onPromptClick)
}

@Composable
private fun GoalLines(state: PreOutingState, onPromptClick: (PreOutingPrompt) -> Unit) {
    if (state.goalLeftLabel != null && state.goalName != null) {
        SentenceRow(text = stringResource(R.string.pre_outing_goal_left, state.goalLeftLabel, state.goalName))
    }
    PromptLines(PreOutingPrompt.PURCHASE_GOAL, state, onPromptClick)
}

@Composable
private fun OutingLines(state: PreOutingState, onPromptClick: (PreOutingPrompt) -> Unit) {
    if (state.monthlyOutingLimit != null) {
        SentenceRow(
            text = stringResource(
                R.string.pre_outing_outings_used,
                state.outingsThisMonth,
                state.monthlyOutingLimit,
            ),
            warning = stringResource(R.string.pre_outing_over_limit).takeIf { state.isNextOutingOverLimit },
        )
    }
    PromptLines(PreOutingPrompt.OUTING_LIMIT, state, onPromptClick)
}

@Composable
private fun PromptLines(
    prompt: PreOutingPrompt,
    state: PreOutingState,
    onPromptClick: (PreOutingPrompt) -> Unit,
) {
    if (prompt in state.prompts) {
        PromptRow(text = stringResource(prompt.labelRes), onClick = { onPromptClick(prompt) })
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun PreOutingContentPreview() {
    AtelierTheme {
        PreOutingContent(
            state = PreOutingState(
                isLoaded = true,
                spentOnOutingsLabel = "S/ 760.00",
                estimatedOutingCostLabel = "S/ 380.00",
                isEstimateAveraged = true,
                debtName = "Banco",
                paymentDay = 15,
                nightPercent = 90,
                goalName = "zapatillas",
                goalLeftLabel = "S/ 270.00",
                outingsThisMonth = 2,
                monthlyOutingLimit = 2,
                isNextOutingOverLimit = true,
                notSpentLabel = "S/ 380.00",
            ),
            onIntent = {},
        )
    }
}

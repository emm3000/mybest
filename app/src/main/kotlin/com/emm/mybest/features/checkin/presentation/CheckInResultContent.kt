package com.emm.mybest.features.checkin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.models.LimitKind
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.LimitOutcomeState
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.message
import com.emm.mybest.ui.theme.AtelierTheme

private val BUTTON_MIN_HEIGHT = 56.dp
private val ROW_VERTICAL_PADDING = 14.dp
private val ROW_GAP = 6.dp

@Composable
fun CheckInResultContent(
    state: CheckInState,
    onIntent: (CheckInIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CheckInPage(modifier = modifier, footer = { ResultFooter(onIntent = onIntent) }) {
        MicroLabel(text = stringResource(R.string.check_in_header, state.dayLabel))
        Text(text = stringResource(R.string.check_in_result_headline), style = MaterialTheme.typography.headlineLarge)
        Column {
            state.outcomes.forEach { OutcomeRow(outcome = it) }
        }
    }
}

@Composable
private fun OutcomeRow(outcome: LimitOutcomeState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = ROW_VERTICAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(ROW_GAP),
    ) {
        MicroLabel(text = stringResource(outcome.kind.labelRes()))
        Text(
            text = outcome.message(),
            style = MaterialTheme.typography.bodyLarge,
            color = outcome.color(),
        )
    }
    Hairline()
}

@Composable
private fun LimitOutcomeState.color() = when (this) {
    is LimitOutcomeState.Kept -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.error
}

private fun LimitKind.labelRes(): Int = when (this) {
    LimitKind.MONEY -> R.string.check_in_field_spent
    LimitKind.DRINKS -> R.string.check_in_field_drinks
    LimitKind.RETURN_TIME -> R.string.check_in_field_return_time
}

@Composable
private fun ColumnScope.ResultFooter(onIntent: (CheckInIntent) -> Unit) {
    Text(
        text = stringResource(R.string.check_in_closing_line),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    HButton(
        onClick = { onIntent(CheckInIntent.OnDoneClick) },
        modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
    ) {
        Text(text = stringResource(R.string.check_in_result_done))
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CheckInResultContentPreview() {
    AtelierTheme {
        CheckInResultContent(
            state = CheckInState(
                isLoaded = true,
                step = CheckInStep.RESULT,
                dayLabel = "VIE · 23 OCT",
                outcomes = listOf(
                    LimitOutcomeState.MoneyExceeded("S/ 60.00"),
                    LimitOutcomeState.DrinksExceeded(1),
                    LimitOutcomeState.Kept(LimitKind.RETURN_TIME),
                ),
            ),
            onIntent = {},
        )
    }
}

package com.emm.mybest.features.checkin.presentation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierTheme

private val BUTTON_MIN_HEIGHT = 56.dp

@Composable
fun CheckInQuestionContent(
    state: CheckInState,
    onIntent: (CheckInIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CheckInPage(modifier = modifier, footer = { QuestionFooter(onIntent = onIntent) }) {
        MicroLabel(text = stringResource(R.string.check_in_header, state.dayLabel))
        Text(text = stringResource(R.string.check_in_question_headline), style = MaterialTheme.typography.headlineLarge)
        Text(
            text = stringResource(R.string.check_in_question_caption),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ColumnScope.QuestionFooter(onIntent: (CheckInIntent) -> Unit) {
    HButton(
        onClick = { onIntent(CheckInIntent.OnWentOutYesClick) },
        modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
        variant = ButtonVariant.Outline,
    ) {
        Text(text = stringResource(R.string.check_in_question_yes))
    }
    HButton(
        onClick = { onIntent(CheckInIntent.OnWentOutNoClick) },
        modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
    ) {
        Text(text = stringResource(R.string.check_in_question_no))
    }
    HButton(
        onClick = { onIntent(CheckInIntent.OnNotNowClick) },
        modifier = Modifier.fillMaxWidth(),
        variant = ButtonVariant.Ghost,
    ) {
        Text(text = stringResource(R.string.check_in_not_now))
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CheckInQuestionContentPreview() {
    AtelierTheme {
        CheckInQuestionContent(
            state = CheckInState(isLoaded = true, step = CheckInStep.QUESTION, dayLabel = "VIE · 23 OCT"),
            onIntent = {},
        )
    }
}

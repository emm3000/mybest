package com.emm.mybest.features.checkin.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.LimitField
import com.emm.mybest.ui.components.LimitInput
import com.emm.mybest.ui.components.LimitLabels
import com.emm.mybest.ui.components.LimitRow
import com.emm.mybest.ui.components.LimitTimeValue
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.datetime.LocalTime

private val BUTTON_MIN_HEIGHT = 56.dp

@Composable
fun CheckInFormContent(
    state: CheckInState,
    onIntent: (CheckInIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CheckInPage(
        modifier = modifier,
        footer = { CheckInFormFooter(canSave = state.canSave, onIntent = onIntent) },
    ) {
        MicroLabel(text = stringResource(R.string.check_in_header, state.dayLabel))
        Text(text = stringResource(R.string.check_in_headline), style = MaterialTheme.typography.headlineLarge)
        CheckInFields(state = state, onIntent = onIntent)
        CheckInMoodSelector(selected = state.form.mood, onSelect = { onIntent(CheckInIntent.OnMoodSelect(it)) })
    }
}

@Composable
private fun CheckInFields(state: CheckInState, onIntent: (CheckInIntent) -> Unit) {
    Column {
        val limits = state.limits
        LimitRow(
            labels = LimitLabels(
                label = stringResource(R.string.check_in_field_spent),
                hint = limits?.let { stringResource(R.string.check_in_limit_hint, it.moneyLabel) }
                    ?: stringResource(R.string.check_in_field_spent_hint_unplanned),
            ),
        ) {
            LimitInput(
                field = LimitField(state.form.spentInput, KeyboardType.Decimal),
                onValueChange = { onIntent(CheckInIntent.OnSpentChange(it)) },
            )
        }
        limits?.let { PlannedFields(limits = it, state = state, onIntent = onIntent) }
    }
}

@Composable
private fun ColumnScope.PlannedFields(
    limits: CheckInLimitsState,
    state: CheckInState,
    onIntent: (CheckInIntent) -> Unit,
) {
    LimitRow(
        labels = LimitLabels(
            label = stringResource(R.string.check_in_field_drinks),
            hint = stringResource(R.string.check_in_limit_hint, limits.drinksLabel),
        ),
    ) {
        LimitInput(
            field = LimitField(state.form.drinksInput, KeyboardType.Number),
            onValueChange = { onIntent(CheckInIntent.OnDrinksChange(it)) },
        )
    }
    LimitRow(
        labels = LimitLabels(
            label = stringResource(R.string.check_in_field_return_time),
            hint = stringResource(R.string.check_in_limit_hint, limits.returnTimeLabel),
        ),
    ) {
        LimitTimeValue(
            text = state.returnTimeLabel ?: stringResource(R.string.check_in_field_return_time_unset),
            onClick = { onIntent(CheckInIntent.OnReturnTimeClick) },
        )
    }
}

@Composable
private fun ColumnScope.CheckInFormFooter(canSave: Boolean, onIntent: (CheckInIntent) -> Unit) {
    Text(
        text = stringResource(R.string.check_in_closing_line),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    HButton(
        onClick = { onIntent(CheckInIntent.OnSaveClick) },
        modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
        enabled = canSave,
    ) {
        Text(text = stringResource(R.string.check_in_save))
    }
    HButton(
        onClick = { onIntent(CheckInIntent.OnNotNowClick) },
        modifier = Modifier.fillMaxWidth(),
        variant = ButtonVariant.Ghost,
    ) {
        Text(text = stringResource(R.string.check_in_not_now))
    }
}

private val PREVIEW_LIMITS = CheckInLimitsState("S/ 200.00", "4", "02:00")
private val PREVIEW_RETURN_TIME = LocalTime(1, 40)

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CheckInFormContentPreview() {
    AtelierTheme {
        CheckInFormContent(
            state = CheckInState(
                isLoaded = true,
                step = CheckInStep.FORM,
                dayLabel = "VIE · 23 OCT",
                limits = PREVIEW_LIMITS,
                form = CheckInForm(
                    spentInput = "260",
                    drinksInput = "5",
                    returnTime = PREVIEW_RETURN_TIME,
                    mood = 2,
                ),
                returnTimeLabel = "01:40",
                canSave = true,
            ),
            onIntent = {},
        )
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CheckInFormContentUnplannedPreview() {
    AtelierTheme {
        CheckInFormContent(
            state = CheckInState(isLoaded = true, step = CheckInStep.FORM, dayLabel = "VIE · 23 OCT"),
            onIntent = {},
        )
    }
}

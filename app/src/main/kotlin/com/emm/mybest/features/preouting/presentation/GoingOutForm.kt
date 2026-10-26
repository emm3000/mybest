package com.emm.mybest.features.preouting.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.LimitField
import com.emm.mybest.ui.components.LimitInput
import com.emm.mybest.ui.components.LimitLabels
import com.emm.mybest.ui.components.LimitRow
import com.emm.mybest.ui.components.LimitTimeValue
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.datetime.LocalTime

private val CONTENT_GUTTER = 24.dp
private val SECTION_GAP = 28.dp
private val BUTTON_MIN_HEIGHT = 56.dp

@Composable
fun GoingOutForm(
    state: PreOutingState,
    onIntent: (PreOutingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CONTENT_GUTTER, vertical = SECTION_GAP),
        verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
    ) {
        MicroLabel(text = stringResource(R.string.pre_outing_form_header, state.tonightLabel))
        Text(text = stringResource(R.string.pre_outing_form_headline), style = MaterialTheme.typography.headlineLarge)
        LimitsSection(state = state, onIntent = onIntent)
        ChecklistSection(state = state, onIntent = onIntent)
        HButton(
            onClick = { onIntent(PreOutingIntent.OnConfirmOutingClick) },
            modifier = Modifier.fillMaxWidth().heightIn(min = BUTTON_MIN_HEIGHT),
            enabled = state.canConfirmOuting,
        ) {
            Text(text = stringResource(R.string.pre_outing_confirm_outing))
        }
    }
}

@Composable
private fun LimitsSection(
    state: PreOutingState,
    onIntent: (PreOutingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        LimitRow(
            labels = LimitLabels(
                label = stringResource(R.string.pre_outing_limit_money),
                hint = stringResource(R.string.pre_outing_limit_money_hint),
            ),
        ) {
            LimitInput(
                field = LimitField(state.form.moneyInput, KeyboardType.Decimal),
                onValueChange = { onIntent(PreOutingIntent.OnMoneyChange(it)) },
            )
        }
        LimitRow(
            labels = LimitLabels(
                label = stringResource(R.string.pre_outing_limit_drinks),
                hint = stringResource(R.string.pre_outing_limit_drinks_hint),
            ),
        ) {
            LimitInput(
                field = LimitField(state.form.drinksInput, KeyboardType.NumberPassword),
                onValueChange = { onIntent(PreOutingIntent.OnDrinksChange(it)) },
            )
        }
        LimitRow(
            labels = LimitLabels(
                label = stringResource(R.string.pre_outing_limit_return_time),
                hint = stringResource(R.string.pre_outing_limit_return_time_hint),
            ),
        ) {
            LimitTimeValue(
                text = state.returnTimeLabel ?: stringResource(R.string.pre_outing_limit_return_time_unset),
                onClick = { onIntent(PreOutingIntent.OnReturnTimeClick) },
            )
        }
    }
}

@Composable
private fun ChecklistSection(
    state: PreOutingState,
    onIntent: (PreOutingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accountText = state.moneyLimitLabel?.let { stringResource(R.string.pre_outing_checklist_account, it) }
        ?: stringResource(R.string.pre_outing_checklist_account_unset)
    Column(modifier = modifier) {
        MicroLabel(text = stringResource(R.string.pre_outing_checklist_label))
        ChecklistRow(
            item = ChecklistItem(stringResource(R.string.pre_outing_checklist_cards), state.form.areCardsOff),
            onToggle = { onIntent(PreOutingIntent.OnCardsOffToggle) },
        )
        ChecklistRow(
            item = ChecklistItem(accountText, state.form.isAccountLoaded),
            onToggle = { onIntent(PreOutingIntent.OnAccountLoadedToggle) },
        )
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun GoingOutFormPreview() {
    AtelierTheme {
        GoingOutForm(
            state = PreOutingState(
                tonightLabel = "VIE · 23 OCT",
                form = PreOutingForm(
                    isOpen = true,
                    moneyInput = "200",
                    drinksInput = "4",
                    returnTime = LocalTime(2, 0),
                    areCardsOff = true,
                ),
                moneyLimitLabel = "S/ 200.00",
                returnTimeLabel = "02:00",
            ),
            onIntent = {},
        )
    }
}

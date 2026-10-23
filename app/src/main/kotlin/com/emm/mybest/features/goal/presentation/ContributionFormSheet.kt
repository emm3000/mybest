package com.emm.mybest.features.goal.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.components.atelier.MicroLabel

private val FORM_GUTTER = 28.dp
private val FORM_GAP = 16.dp

@Composable
internal fun ContributionFormSheet(
    form: ContributionFormState,
    onIntent: (PurchaseGoalIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HBottomSheet(
        onDismissRequest = { onIntent(PurchaseGoalIntent.OnContributionDismiss) },
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = FORM_GUTTER, vertical = FORM_GAP),
            verticalArrangement = Arrangement.spacedBy(FORM_GAP),
        ) {
            MicroLabel(text = stringResource(R.string.purchase_goal_contribution_form_title))
            HInput(
                value = form.amount,
                onValueChange = { onIntent(PurchaseGoalIntent.OnContributionChange(it)) },
                label = stringResource(R.string.purchase_goal_field_contribution),
                errorMessage = form.error?.let { stringResource(it.messageRes()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            ContributionFormButtons(onIntent = onIntent)
        }
    }
}

@Composable
private fun ContributionFormButtons(onIntent: (PurchaseGoalIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(FORM_GAP)) {
        HButton(
            text = stringResource(R.string.purchase_goal_contribution_save),
            onClick = { onIntent(PurchaseGoalIntent.OnContributionSave) },
        )
        HButton(
            text = stringResource(R.string.purchase_goal_cancel),
            onClick = { onIntent(PurchaseGoalIntent.OnContributionDismiss) },
            variant = ButtonVariant.Outline,
        )
    }
}

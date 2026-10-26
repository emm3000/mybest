package com.emm.mybest.features.goal.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.validation.PurchaseGoalValidationError
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HInput
import com.emm.mybest.ui.theme.AtelierTheme

private val SECTION_GAP = 16.dp

@Composable
internal fun DefineGoalSection(
    form: GoalFormState,
    onIntent: (PurchaseGoalIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SECTION_GAP)) {
        Text(text = stringResource(form.titleRes()), style = MaterialTheme.typography.titleMedium)
        GoalFormField.entries.forEach { field -> GoalFormInput(form = form, field = field, onIntent = onIntent) }
        HButton(
            text = stringResource(R.string.purchase_goal_save),
            onClick = { onIntent(PurchaseGoalIntent.OnGoalSave) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (form.isEditing) {
            HButton(
                text = stringResource(R.string.purchase_goal_cancel),
                onClick = { onIntent(PurchaseGoalIntent.OnEditCancel) },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Outline,
            )
        }
    }
}

private fun GoalFormState.titleRes(): Int =
    if (isEditing) R.string.purchase_goal_edit_title else R.string.purchase_goal_define_title

@Composable
private fun GoalFormInput(
    form: GoalFormState,
    field: GoalFormField,
    onIntent: (PurchaseGoalIntent) -> Unit,
) {
    HInput(
        value = form.valueOf(field),
        onValueChange = { onIntent(PurchaseGoalIntent.OnGoalChange(field, it)) },
        label = stringResource(field.labelRes),
        errorMessage = form.errorFor(field)?.let { stringResource(it.messageRes()) },
        keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
    )
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun DefineGoalSectionPreview() {
    AtelierTheme {
        DefineGoalSection(
            form = GoalFormState(name = "", price = "0", errors = setOf(PurchaseGoalValidationError.BlankName)),
            onIntent = {},
        )
    }
}

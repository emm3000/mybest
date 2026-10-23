package com.emm.mybest.features.debts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
internal fun DebtFormSheet(
    form: DebtFormState,
    onIntent: (DebtsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HBottomSheet(
        onDismissRequest = { onIntent(DebtsIntent.OnFormDismiss) },
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = FORM_GUTTER, vertical = FORM_GAP),
            verticalArrangement = Arrangement.spacedBy(FORM_GAP),
        ) {
            MicroLabel(text = stringResource(form.titleRes()))
            DebtFormField.entries.forEach { field -> DebtFormInput(form = form, field = field, onIntent = onIntent) }
            DebtFormButtons(onIntent = onIntent)
        }
    }
}

@Composable
private fun DebtFormInput(
    form: DebtFormState,
    field: DebtFormField,
    onIntent: (DebtsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HInput(
        modifier = modifier,
        value = form.valueOf(field),
        onValueChange = { onIntent(DebtsIntent.OnFormChange(field, it)) },
        label = stringResource(field.labelRes),
        errorMessage = form.errorFor(field)?.let { stringResource(it.messageRes()) },
        keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
    )
}

@Composable
private fun DebtFormButtons(onIntent: (DebtsIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(FORM_GAP)) {
        HButton(text = stringResource(R.string.debts_form_save), onClick = { onIntent(DebtsIntent.OnFormSave) })
        HButton(
            text = stringResource(R.string.debts_form_cancel),
            onClick = { onIntent(DebtsIntent.OnFormDismiss) },
            variant = ButtonVariant.Outline,
        )
    }
}

private fun DebtFormState.titleRes(): Int =
    if (id == 0L) R.string.debts_form_title_add else R.string.debts_form_title_edit

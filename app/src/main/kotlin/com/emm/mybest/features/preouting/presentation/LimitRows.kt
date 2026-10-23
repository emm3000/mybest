package com.emm.mybest.features.preouting.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.CompletionCheck
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel

private val ROW_VERTICAL_PADDING = 12.dp
private val ROW_GAP = 16.dp
private val LABEL_GAP = 6.dp
private val INPUT_WIDTH = 150.dp
private val CHECK_VERTICAL_PADDING = 14.dp
private val CHECK_GAP = 14.dp

@Composable
internal fun LimitRow(
    labels: LimitLabels,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = ROW_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LABEL_GAP)) {
            MicroLabel(text = labels.label)
            Text(
                text = labels.hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        value()
    }
    Hairline()
}

@Composable
internal fun LimitInput(
    field: LimitField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.displaySmall
    BasicTextField(
        value = field.value,
        onValueChange = onValueChange,
        modifier = modifier.width(INPUT_WIDTH),
        textStyle = style.copy(color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerField ->
            if (field.value.isEmpty()) {
                Text(
                    text = stringResource(R.string.pre_outing_limit_input_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                    style = style.copy(textAlign = TextAlign.End),
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            innerField()
        },
    )
}

@Composable
internal fun LimitTimeValue(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.clickable(onClick = onClick),
        style = MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.End,
    )
}

@Composable
internal fun ChecklistRow(
    item: ChecklistItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = item.isChecked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(vertical = CHECK_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(CHECK_GAP),
        verticalAlignment = Alignment.Top,
    ) {
        CompletionCheck(done = item.isChecked)
        Text(text = item.text, style = MaterialTheme.typography.bodyLarge)
    }
}

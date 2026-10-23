package com.emm.mybest.features.preouting.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.atelier.CompletionCheck

private val CHECK_VERTICAL_PADDING = 14.dp
private val CHECK_GAP = 14.dp

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

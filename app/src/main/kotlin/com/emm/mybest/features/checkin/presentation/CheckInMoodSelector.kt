package com.emm.mybest.features.checkin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.domain.models.CHECK_IN_MOOD_RANGE
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.theme.AtelierTheme

private val SECTION_GAP = 12.dp
private val CELL_VERTICAL_PADDING = 14.dp
private val BORDER_WIDTH = 1.dp

@Composable
internal fun CheckInMoodSelector(
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SECTION_GAP)) {
        MicroLabel(text = stringResource(R.string.check_in_mood_label))
        Row(modifier = Modifier.fillMaxWidth().border(BORDER_WIDTH, MaterialTheme.colorScheme.outline)) {
            CHECK_IN_MOOD_RANGE.forEach { mood ->
                MoodCell(
                    mood = mood,
                    isSelected = mood == selected,
                    onSelect = { onSelect(mood) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MoodCaption(text = stringResource(R.string.check_in_mood_low))
            MoodCaption(text = stringResource(R.string.check_in_mood_high))
        }
    }
}

@Composable
private fun MoodCell(
    mood: Int,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(R.string.check_in_mood_option, mood)
    Box(
        modifier = modifier
            .background(if (isSelected) colors.onSurface else colors.background)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onSelect)
            .semantics { contentDescription = description }
            .padding(vertical = CELL_VERTICAL_PADDING),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mood.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) colors.background else colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun MoodCaption(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun CheckInMoodSelectorPreview() {
    AtelierTheme {
        CheckInMoodSelector(selected = 2, onSelect = {}, modifier = Modifier.padding(24.dp))
    }
}

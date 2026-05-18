package com.emm.mybest.features.exercise.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.StarlinkTextStyles
import kotlinx.datetime.DayOfWeek

private const val DAY_LABEL_WEIGHT = 0.35f
private const val ROUTINE_WEIGHT = 0.65f

@Composable
fun ExerciseDayRow(
    day: DayOfWeek,
    routine: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = day.name,
            style = StarlinkTextStyles.sectionLabel,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(DAY_LABEL_WEIGHT),
        )
        val hasContent = routine.isNotEmpty()
        Text(
            text = if (hasContent) routine else "—",
            style = MaterialTheme.typography.bodyLarge,
            color = if (hasContent) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(ROUTINE_WEIGHT),
        )
    }
}

package com.emm.mybest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitType

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: Habit,
    record: HabitRecord?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = record?.isCompleted ?: false
    val cs = MaterialTheme.colorScheme

    @Suppress("DEPRECATION")
    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it != androidx.compose.material3.SwipeToDismissBoxValue.Settled) {
                onToggle()
                false // Don't actually dismiss, just trigger action and snap back
            } else {
                false
            }
        }
    )

    androidx.compose.material3.SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            HabitCardSwipeBackground(isCompleted = isCompleted)
        },
        modifier = modifier,
    ) {
        HCard(
            modifier = Modifier.fillMaxWidth(),
            variant = if (isCompleted) CardVariant.Outlined else CardVariant.Elevated,
        ) {
            HabitCardContent(
                habit = habit,
                record = record,
                isCompleted = isCompleted,
                onToggle = onToggle,
            )
        }
    }
}

@Composable
private fun HabitCardSwipeBackground(isCompleted: Boolean) {
    val cs = MaterialTheme.colorScheme
    val backgroundColor = if (isCompleted) cs.errorContainer else cs.primaryContainer
    val iconTint = if (isCompleted) cs.onErrorContainer else cs.onPrimaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(
            imageVector = completionIcon(isCompleted = isCompleted),
            contentDescription = null,
            tint = iconTint,
        )
    }
}

@Composable
private fun HabitCardContent(
    habit: Habit,
    record: HabitRecord?,
    isCompleted: Boolean,
    onToggle: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HabitCardLeadingIcon(color = habit.color)
        Spacer(modifier = Modifier.width(16.dp))
        HabitCardTexts(
            habit = habit,
            record = record,
            isCompleted = isCompleted,
            modifier = Modifier.weight(1f),
        )
        CompletionToggle(
            isCompleted = isCompleted,
            onToggle = onToggle,
            tint = if (isCompleted) cs.primary else cs.outline,
        )
    }
}

@Composable
private fun HabitCardLeadingIcon(color: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(color).copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = Color(color),
        )
    }
}

@Composable
private fun HabitCardTexts(
    habit: Habit,
    record: HabitRecord?,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Text(
            text = habit.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isCompleted) cs.onSurface.copy(alpha = 0.6f) else cs.onSurface,
        )
        Text(
            text = habit.secondaryText(record),
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
        )
    }
}

@Composable
private fun CompletionToggle(
    isCompleted: Boolean,
    onToggle: () -> Unit,
    tint: Color,
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = completionIcon(isCompleted = isCompleted),
            contentDescription = completionLabel(isCompleted = isCompleted),
            tint = tint,
        )
    }
}

private fun completionIcon(isCompleted: Boolean) = if (isCompleted) {
    Icons.Rounded.CheckCircle
} else {
    Icons.Rounded.RadioButtonUnchecked
}

private fun completionLabel(isCompleted: Boolean): String = if (isCompleted) {
    "Completado"
} else {
    "Marcar como completado"
}

private fun Habit.secondaryText(record: HabitRecord?): String {
    if (type == HabitType.BOOLEAN) return category
    val goalText = goalValue?.toInt()?.toString() ?: "0"
    val currentValue = record?.value?.toInt() ?: 0
    val unitText = unit ?: ""
    return "$currentValue / $goalText $unitText"
}

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
            val color = if (isCompleted) cs.errorContainer else cs.primaryContainer
            val alignment = Alignment.CenterStart

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = if (isCompleted) {
                        Icons.Rounded.RadioButtonUnchecked
                    } else {
                        Icons.Rounded.CheckCircle
                    },
                    contentDescription = null,
                    tint = if (isCompleted) cs.onErrorContainer else cs.onPrimaryContainer
                )
            }
        },
        modifier = modifier
    ) {
        HCard(
            modifier = Modifier.fillMaxWidth(),
            variant = if (isCompleted) CardVariant.Outlined else CardVariant.Elevated
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(habit.color).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    // In a real app we'd map string icon names to ImageVectors
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(habit.color)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) cs.onSurface.copy(alpha = 0.6f) else cs.onSurface
                    )

                    if (habit.type != HabitType.BOOLEAN) {
                        val progressText = if (record != null) {
                            "${record.value.toInt()} / ${habit.goalValue?.toInt()} ${habit.unit ?: ""}"
                        } else {
                            "0 / ${habit.goalValue?.toInt()} ${habit.unit ?: ""}"
                        }
                        Text(
                            text = progressText,
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = habit.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isCompleted) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.RadioButtonUnchecked
                        },
                        contentDescription = if (isCompleted) {
                            "Completado"
                        } else {
                            "Marcar como completado"
                        },
                        tint = if (isCompleted) cs.primary else cs.outline
                    )
                }
            }
        }
    }
}

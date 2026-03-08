package com.emm.mybest.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.viewmodel.HomeIntent
import com.emm.mybest.viewmodel.HomeState

internal fun LazyListScope.homeHabitsSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    item {
        Text(
            text = "Hábitos de hoy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
    }

    if (state.dailyHabits.isEmpty()) {
        item {
            HEmptyState(
                title = "Sin hábitos para hoy",
                description = "Crea un nuevo hábito para empezar a trackear tu progreso.",
                icon = Icons.Rounded.CheckCircle,
                action = {
                    TextButton(onClick = { onIntent(HomeIntent.OnAddHabitClick) }) {
                        Text("Añadir Hábito")
                    }
                },
            )
        }
    } else {
        items(state.dailyHabits) { habitWithRecord ->
            com.emm.mybest.ui.components.HabitCard(
                habit = habitWithRecord.habit,
                record = habitWithRecord.record,
                onToggle = { onIntent(HomeIntent.ToggleHabit(habitWithRecord)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun QuickAccessHeader(
    onTimelineClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Accesos rápidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row {
            TextButton(onClick = onTimelineClick) { Text("Timeline") }
            TextButton(onClick = onInsightsClick) { Text("Estadísticas") }
            TextButton(onClick = onHistoryClick) { Text("Historial") }
        }
    }
}

@Composable
internal fun WeightQuickAction(
    state: HomeState,
    onClick: () -> Unit,
) {
    QuickActionCard(
        title = "Registrar Peso",
        subtitle = state.lastWeight?.let { "Último: $it kg" } ?: "Sigue tu evolución física",
        icon = Icons.Rounded.MonitorWeight,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
    )
}

@Composable
internal fun DailyHabitsQuickAction(
    state: HomeState,
    onClick: () -> Unit,
) {
    val subtitle = if (state.dailyHabits.count { it.record?.isCompleted == true } > 0) {
        "¡Ya has registrado hoy!"
    } else {
        "¿Qué tal tu alimentación?"
    }

    QuickActionCard(
        title = "Hábitos de Hoy",
        subtitle = subtitle,
        icon = Icons.Rounded.CheckCircle,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        iconColor = MaterialTheme.colorScheme.secondary,
        onClick = onClick,
    )
}

@Composable
internal fun ProgressPhotoQuickAction(
    state: HomeState,
    onClick: () -> Unit,
) {
    QuickActionCard(
        title = "Foto de Progreso",
        subtitle = "Total: ${state.totalPhotos} fotos",
        icon = Icons.Rounded.AddAPhoto,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        iconColor = MaterialTheme.colorScheme.tertiary,
        onClick = onClick,
    )
}

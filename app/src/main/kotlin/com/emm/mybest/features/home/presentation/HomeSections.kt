package com.emm.mybest.features.home.presentation

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HabitCard

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
                    HButton(
                        text = "Añadir Hábito",
                        onClick = { onIntent(HomeIntent.OnAddHabitClick) },
                    )
                },
            )
        }
    } else {
        items(state.dailyHabits) { habitWithRecord ->
            HabitCard(
                habit = habitWithRecord.habit,
                record = habitWithRecord.record,
                onToggle = { onIntent(HomeIntent.ToggleHabit(habitWithRecord)) },
                modifier = Modifier.fillMaxWidth(),
            )
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
internal fun NewHabitQuickAction(
    state: HomeState,
    onClick: () -> Unit,
) {
    val subtitle = if (state.dailyHabits.isEmpty()) {
        "Define tu primer hábito diario"
    } else {
        "Añade otro hábito a tu seguimiento"
    }

    QuickActionCard(
        title = "Crear Hábito",
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

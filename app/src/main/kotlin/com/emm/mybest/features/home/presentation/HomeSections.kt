package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val HOME_PRIMARY_ACTIONS_SPACING = 12

internal fun LazyListScope.homePrimaryActionsSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    item {
        Column(
            verticalArrangement = Arrangement.spacedBy(HOME_PRIMARY_ACTIONS_SPACING.dp),
        ) {
            Text(
                text = "Acciones de apoyo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            WeightQuickAction(
                state = state,
                onClick = { onIntent(HomeIntent.OnAddWeightClick) },
            )
            ProgressPhotoQuickAction(
                state = state,
                onClick = { onIntent(HomeIntent.OnAddPhotoClick) },
            )
            NewHabitQuickAction(
                state = state,
                onClick = { onIntent(HomeIntent.OnAddHabitClick) },
            )
            ReminderSettingsQuickAction(
                onClick = { onIntent(HomeIntent.OnReminderSettingsClick) },
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
        title = "Registrar peso",
        subtitle = homeWeightSubtitle(state),
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
    QuickActionCard(
        title = "Crear un hábito",
        subtitle = homeNewHabitSubtitle(state),
        icon = Icons.Rounded.CheckCircle,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick = onClick,
    )
}

@Composable
internal fun ProgressPhotoQuickAction(
    state: HomeState,
    onClick: () -> Unit,
) {
    QuickActionCard(
        title = "Registrar foto de progreso",
        subtitle = homeProgressPhotoSubtitle(state),
        icon = Icons.Rounded.AddAPhoto,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        iconColor = MaterialTheme.colorScheme.tertiary,
        onClick = onClick,
    )
}

@Composable
internal fun ReminderSettingsQuickAction(
    onClick: () -> Unit,
) {
    QuickActionCard(
        title = "Configurar recordatorios",
        subtitle = "Activa o pausa avisos preventivos por hábito",
        icon = Icons.Rounded.NotificationsActive,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.primary,
        onClick = onClick,
    )
}

private fun homeWeightSubtitle(state: HomeState): String {
    if (state.isLoading) return "Cargando tu último registro"
    return state.lastWeight?.let { "Último registro: $it kg" } ?: "Evidencia tu avance con este dato"
}

private fun homeNewHabitSubtitle(state: HomeState): String {
    if (state.isLoading) return "Preparando tu rutina de hoy"
    return if (state.dailyHabits.isEmpty()) {
        "Configura tu primer hábito para los próximos días"
    } else {
        "Crea otro hábito para tu rutina semanal"
    }
}

private fun homeProgressPhotoSubtitle(state: HomeState): String {
    if (state.isLoading) return "Cargando tu historial visual"
    return "Evidencias registradas: ${state.totalPhotos} fotos"
}

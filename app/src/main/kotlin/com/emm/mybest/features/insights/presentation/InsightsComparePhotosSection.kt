package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton

@Composable
internal fun InsightsComparePhotosSection(
    state: InsightsState,
    periodLabel: String,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InsightsSection(
        title = "Comparación visual",
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            androidx.compose.material3.Text(
                text = if (state.canComparePhotos) {
                    "Compara tus fotos de progreso para ver cambios físicos junto a tus métricas."
                } else {
                    "Aún necesitas al menos 2 fotos de progreso para usar el comparador visual."
                },
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.material3.Text(
                text = periodLabel,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
            )
            HButton(
                text = if (state.canComparePhotos) "Abrir comparador" else "Comparador no disponible",
                onClick = onCompareClick,
                enabled = state.canComparePhotos,
                leadingIcon = Icons.Rounded.Compare,
                variant = if (state.canComparePhotos) {
                    ButtonVariant.Secondary
                } else {
                    ButtonVariant.Outline
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

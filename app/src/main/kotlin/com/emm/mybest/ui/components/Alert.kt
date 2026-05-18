package com.emm.mybest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.ui.theme.StarlinkTextStyles

// ─── Variants ──────────────────────────────────────────────────────────────

enum class AlertVariant { Default, Destructive, Warning, Success }

/**
 * Alert / callout inspired by shadcn/ui.
 *
 * Usage: error messages in NewCardScreen ([AlertVariant.Destructive]),
 *        general information, action confirmations.
 */
@Composable
fun HAlert(
    title: String,
    modifier: Modifier = Modifier,
    variant: AlertVariant = AlertVariant.Default,
    description: String? = null,
    icon: ImageVector? = null,
) {
    val cs = MaterialTheme.colorScheme
    val barColor = alertBarColor(variant)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = cs.surface,
        contentColor = cs.onSurface,
        border = BorderStroke(1.dp, cs.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.height(androidx.compose.ui.unit.Dp.Unspecified),
            verticalAlignment = Alignment.Top,
        ) {
            // Leading 2dp vertical accent bar
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(barColor),
            )
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                val resolvedIcon = icon ?: when (variant) {
                    AlertVariant.Destructive -> Icons.Default.Warning
                    AlertVariant.Warning -> Icons.Default.Warning
                    else -> Icons.Default.Info
                }
                Icon(
                    imageVector = resolvedIcon,
                    contentDescription = null,
                    tint = barColor,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 1.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title.uppercase(),
                        style = StarlinkTextStyles.sectionLabel,
                        color = cs.onSurface,
                    )
                    if (description != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun alertBarColor(variant: AlertVariant): Color {
    val cs = MaterialTheme.colorScheme
    return when (variant) {
        AlertVariant.Default -> cs.onSurfaceVariant
        AlertVariant.Destructive -> cs.error
        AlertVariant.Warning -> cs.onSurface
        AlertVariant.Success -> cs.tertiary
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@PreviewLightDark
@Composable
private fun HAlertVariantsPreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HAlert(
                    title = "Información",
                    description = "Revisa los datos antes de continuar.",
                    variant = AlertVariant.Default,
                )
                HAlert(
                    title = "Error al generar",
                    description = "No se pudo conectar con el servidor. Verifica tu conexión.",
                    variant = AlertVariant.Destructive,
                )
                HAlert(
                    title = "Atención",
                    description = "Esta acción no se puede deshacer.",
                    variant = AlertVariant.Warning,
                )
                HAlert(
                    title = "Tarjeta guardada",
                    description = "La flashcard fue creada y añadida a tu mazo.",
                    variant = AlertVariant.Success,
                )
            }
        }
    }
}

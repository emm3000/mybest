package com.emm.mybest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.theme.MyBestTheme

enum class StatChipVariant { Neutral, Primary, Secondary, Tertiary, Success, Destructive }

@Composable
fun HStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    variant: StatChipVariant = StatChipVariant.Neutral,
    compact: Boolean = false,
) {
    val cs = MaterialTheme.colorScheme
    val (containerColor, valueColor, labelColor) = statChipTokens(variant)

    if (compact) {
        Row(
            modifier = modifier
                .background(
                    color = containerColor,
                    shape = CircleShape,
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor.copy(alpha = 0.95f),
            )
        }
        return
    }

    Column(
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun statChipTokens(variant: StatChipVariant): Triple<Color, Color, Color> {
    val cs = MaterialTheme.colorScheme
    return when (variant) {
        StatChipVariant.Neutral -> Triple(
            cs.surfaceContainerLow,
            cs.onSurface,
            cs.onSurfaceVariant,
        )
        StatChipVariant.Primary -> Triple(
            cs.primaryContainer,
            cs.onPrimaryContainer,
            cs.onPrimaryContainer.copy(alpha = 0.85f),
        )
        StatChipVariant.Secondary -> Triple(
            cs.secondaryContainer,
            cs.onSecondaryContainer,
            cs.onSecondaryContainer.copy(alpha = 0.85f),
        )
        StatChipVariant.Tertiary -> Triple(
            cs.tertiaryContainer,
            cs.onTertiaryContainer,
            cs.onTertiaryContainer.copy(alpha = 0.85f),
        )
        StatChipVariant.Success -> Triple(
            cs.tertiaryContainer,
            cs.onTertiaryContainer,
            cs.onTertiaryContainer.copy(alpha = 0.85f),
        )
        StatChipVariant.Destructive -> Triple(
            cs.errorContainer,
            cs.onErrorContainer,
            cs.onErrorContainer.copy(alpha = 0.85f),
        )
    }
}

@PreviewLightDark
@Composable
private fun HStatChipPreview() {
    MyBestTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HStatChip(label = "Total", value = "12", modifier = Modifier.weight(1f))
                    HStatChip(
                        label = "Completados",
                        value = "9",
                        variant = StatChipVariant.Primary,
                        modifier = Modifier.weight(1f),
                    )
                    HStatChip(
                        label = "Pendientes",
                        value = "3",
                        variant = StatChipVariant.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HStatChip(
                        label = "Ejercicio",
                        value = "4 días",
                        variant = StatChipVariant.Tertiary,
                        compact = true,
                        modifier = Modifier.wrapContentWidth(),
                    )
                    HStatChip(
                        label = "Errores",
                        value = "1",
                        variant = StatChipVariant.Destructive,
                        compact = true,
                        modifier = Modifier.wrapContentWidth(),
                    )
                }
            }
        }
    }
}

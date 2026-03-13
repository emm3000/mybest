package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val HISTORY_LEGEND_DOT_SIZE = 8
private const val HISTORY_LEGEND_SECTION_CORNER = 16

@Composable
fun LegendItem(
    color: Color,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(HISTORY_LEGEND_DOT_SIZE.dp).clip(CircleShape).background(color))
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HistoryLegendSection(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(HISTORY_LEGEND_SECTION_CORNER.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Leyenda",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, text = "Peso")
                LegendItem(color = MaterialTheme.colorScheme.secondary, text = "Hábitos")
                LegendItem(color = MaterialTheme.colorScheme.tertiary, text = "Fotos")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LegendItem(color = dayIntensityColor(DayIntensity.LOW), text = "Baja")
                LegendItem(color = dayIntensityColor(DayIntensity.MEDIUM), text = "Media")
                LegendItem(color = dayIntensityColor(DayIntensity.HIGH), text = "Alta")
            }
        }
    }
}

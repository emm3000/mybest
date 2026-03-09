package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.North
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.formatEsLongDate
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HAlert
import com.emm.mybest.ui.components.HCard
import java.util.Locale
import kotlin.math.abs

private const val WEIGHT_SECTION_SPACING = 12
private const val WEIGHT_SECTION_CORNER = 20
private const val WEIGHT_CHART_HEIGHT = 250

@Composable
internal fun WeightInsightsSection(
    state: InsightsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WEIGHT_SECTION_SPACING.dp),
    ) {
        WeightSummaryCards(state = state)

        InsightsSection(title = "Evolución de Peso") {
            Column(verticalArrangement = Arrangement.spacedBy(WEIGHT_SECTION_SPACING.dp)) {
                Text(
                    text = weightPeriodText(state.weightHistory),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WeightChart(
                    weights = state.weightHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WEIGHT_CHART_HEIGHT.dp),
                )
                Text(
                    text = weightTrendSummary(
                        weights = state.weightHistory,
                        totalWeightLost = state.totalWeightLost,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun ComparePhotosAvailabilityNote(
    photoCount: Int,
    modifier: Modifier = Modifier,
) {
    if (photoCount >= MIN_COMPARE_PHOTOS) {
        return
    }

    HAlert(
        title = "Comparación no disponible",
        description = "Necesitas al menos 2 fotos de progreso para habilitar el comparador.",
        modifier = modifier,
    )
}

@Composable
private fun WeightSummaryCards(
    state: InsightsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WEIGHT_SECTION_SPACING.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(WEIGHT_SECTION_SPACING.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Actual",
                value = formatWeight(state.currentWeight),
                icon = Icons.Rounded.MonitorWeight,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = weightDeltaTitle(state.totalWeightLost),
                value = formatWeight(abs(state.totalWeightLost)),
                icon = if (state.totalWeightLost >= 0f) {
                    Icons.AutoMirrored.Rounded.TrendingDown
                } else {
                    Icons.Rounded.North
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            )
        }
        Text(
            text = weightSummaryContextText(state.weightHistory),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    HCard(
        modifier = modifier,
        variant = CardVariant.Filled,
        cornerRadius = WEIGHT_SECTION_CORNER.dp,
        containerColor = containerColor,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun WeightChart(
    weights: List<WeightEntry>,
    modifier: Modifier = Modifier,
) {
    if (weights.size < MIN_COMPARE_PHOTOS) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Registra al menos 2 pesos para ver el gráfico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val pointInnerColor = MaterialTheme.colorScheme.background

    Canvas(modifier = modifier) {
        val minWeight = weights.minOf { it.weight } - 2f
        val maxWeight = weights.maxOf { it.weight } + 2f
        val range = maxWeight - minWeight

        val width = size.width
        val height = size.height
        val stepX = width / (weights.size - 1)

        val points = weights.mapIndexed { index, dailyWeight ->
            val x = index * stepX
            val y = height - ((dailyWeight.weight - minWeight) / range * height)
            Offset(x, y)
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.forEach { lineTo(it.x, it.y) }
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = height,
            ),
        )

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4.dp.toPx()),
        )

        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = pointInnerColor,
                radius = 2.dp.toPx(),
                center = point,
            )
        }
    }
}

private fun weightSummaryContextText(weights: List<WeightEntry>): String {
    if (weights.isEmpty()) {
        return "Contexto: aún no hay registros de peso."
    }

    return "Contexto: ${weights.first().date.formatEsLongDate()} a ${weights.last().date.formatEsLongDate()}"
}

private fun weightPeriodText(weights: List<WeightEntry>): String {
    if (weights.isEmpty()) {
        return "Todavía no hay registros de peso para este gráfico."
    }

    return "Periodo mostrado: ${weights.first().date.formatEsLongDate()} a ${weights.last().date.formatEsLongDate()}"
}

private fun weightTrendSummary(
    weights: List<WeightEntry>,
    totalWeightLost: Float,
): String = when {
    weights.size >= MIN_COMPARE_PHOTOS && totalWeightLost > 0f ->
        "Has bajado ${formatWeight(abs(totalWeightLost))} desde tu primer registro."
    weights.size >= MIN_COMPARE_PHOTOS && totalWeightLost < 0f ->
        "Has subido ${formatWeight(abs(totalWeightLost))} desde tu primer registro."
    weights.size >= MIN_COMPARE_PHOTOS ->
        "Tu peso se mantiene estable frente al primer registro."
    weights.size == 1 ->
        "Solo hay un registro de peso por ahora; aún no se puede mostrar tendencia."
    else ->
        "Agrega registros de peso para obtener una lectura de tendencia."
}

private fun weightDeltaTitle(totalWeightLost: Float): String =
    if (totalWeightLost >= 0f) "Pérdida" else "Aumento"

private fun formatWeight(weight: Float): String =
    String.format(Locale.getDefault(), "%.1f kg", weight)

private const val MIN_COMPARE_PHOTOS = 2

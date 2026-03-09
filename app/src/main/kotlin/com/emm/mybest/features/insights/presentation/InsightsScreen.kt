package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.North
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.ui.components.AlertVariant
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HAlert
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HSkeleton
import com.emm.mybest.ui.components.HTopBar

private const val INSIGHTS_SCREEN_PADDING = 16
private const val INSIGHTS_SECTION_SPACING = 16
private const val INSIGHTS_SECTION_CORNER = 20
private const val INSIGHTS_SECTION_CONTENT_PADDING = 16
private const val INSIGHTS_CHART_HEIGHT = 250
private const val INSIGHTS_STATS_GAP = 16
private const val INSIGHTS_RING_SIZE = 80

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onBackClick: () -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    HandleInsightsEffects(
        viewModel = viewModel,
        onBackClick = onBackClick,
        onCompareClick = onCompareClick,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            HTopBar(
                title = "Insights & Progreso",
                navigationIcon = {
                    IconButton(onClick = { viewModel.onIntent(InsightsIntent.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(InsightsIntent.OnCompareClick) }) {
                        Icon(Icons.Rounded.Compare, contentDescription = "Comparar fotos")
                    }
                },
            )
        },
    ) { padding -> InsightsBody(state = state, padding = padding) }
}

@Composable
private fun HandleInsightsEffects(
    viewModel: InsightsViewModel,
    onBackClick: () -> Unit,
    onCompareClick: () -> Unit,
) {
    val currentOnBackClick by androidx.compose.runtime.rememberUpdatedState(onBackClick)
    val currentOnCompareClick by androidx.compose.runtime.rememberUpdatedState(onCompareClick)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                InsightsEffect.NavigateBack -> currentOnBackClick()
                InsightsEffect.NavigateToCompare -> currentOnCompareClick()
            }
        }
    }
}

@Composable
private fun InsightsBody(
    state: InsightsState,
    padding: androidx.compose.foundation.layout.PaddingValues,
) {
    val contentModifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .padding(INSIGHTS_SCREEN_PADDING.dp)

    when {
        state.isLoading -> InsightsLoadingState(modifier = contentModifier)
        state.errorMessage != null -> {
            HAlert(
                title = "No se pudieron cargar los insights",
                description = state.errorMessage,
                variant = AlertVariant.Destructive,
                modifier = contentModifier,
            )
        }
        state.weightHistory.isEmpty() && state.exerciseDays == 0 && state.healthyEatingDays == 0 -> {
            HEmptyState(
                title = "Sin datos para insights",
                description = "Registra peso y hábitos para ver tu progreso en esta pantalla.",
                icon = Icons.Rounded.BarChart,
                modifier = contentModifier,
            )
        }
        else -> InsightsDataContent(state = state, modifier = contentModifier)
    }
}

@Composable
private fun InsightsDataContent(
    state: InsightsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_SECTION_SPACING.dp),
    ) {
        WeightSummaryCards(state)

        InsightsSection(title = "Evolución de Peso") {
            WeightChart(
                weights = state.weightHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(INSIGHTS_CHART_HEIGHT.dp)
                    .padding(top = INSIGHTS_SECTION_CONTENT_PADDING.dp),
            )
        }

        InsightsSection(title = "Consistencia de Hábitos") {
            HabitStats(state)
        }
    }
}

@Composable
private fun InsightsLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_SECTION_SPACING.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HSkeleton(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                cornerRadius = INSIGHTS_SECTION_CORNER.dp,
            )
            HSkeleton(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                cornerRadius = INSIGHTS_SECTION_CORNER.dp,
            )
        }
        HSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(INSIGHTS_CHART_HEIGHT.dp),
            cornerRadius = INSIGHTS_SECTION_CORNER.dp,
        )
        HSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            cornerRadius = INSIGHTS_SECTION_CORNER.dp,
        )
    }
}

@Composable
private fun InsightsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        HCard(
            variant = CardVariant.Filled,
            cornerRadius = INSIGHTS_SECTION_CORNER.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(modifier = Modifier.padding(INSIGHTS_SECTION_CONTENT_PADDING.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun WeightSummaryCards(
    state: InsightsState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Actual",
            value = "${state.currentWeight}kg",
            icon = Icons.Rounded.MonitorWeight,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Pérdida",
            value = String.format(java.util.Locale.getDefault(), "%.1fkg", state.totalWeightLost),
            icon = if (state.totalWeightLost >= 0) Icons.AutoMirrored.Rounded.TrendingDown else Icons.Rounded.North,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    HCard(
        modifier = modifier,
        variant = CardVariant.Filled,
        cornerRadius = INSIGHTS_SECTION_CORNER.dp,
        containerColor = containerColor,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun WeightChart(
    weights: List<WeightEntry>,
    modifier: Modifier = Modifier,
) {
    if (weights.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                "Registra al menos 2 pesos para ver el gráfico",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary

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

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point,
            )
        }
    }
}

@Composable
private fun HabitStats(
    state: InsightsState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_STATS_GAP.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(INSIGHTS_RING_SIZE.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { state.habitConsistency },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                )
                Text(
                    text = "${(state.habitConsistency * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(INSIGHTS_SECTION_SPACING.dp))
            Column {
                Text("Consistencia General", fontWeight = FontWeight.Bold)
                Text(
                    "Basado en todos tus registros",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }

        HorizontalStatRow(
            label = "Días de Ejercicio",
            count = state.exerciseDays,
            color = MaterialTheme.colorScheme.secondary,
        )
        HorizontalStatRow(
            label = "Comida Saludable",
            count = state.healthyEatingDays,
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

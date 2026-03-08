package com.emm.mybest.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.North
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.emm.mybest.viewmodel.InsightsEffect
import com.emm.mybest.viewmodel.InsightsIntent
import com.emm.mybest.viewmodel.InsightsState
import com.emm.mybest.viewmodel.InsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onBackClick: () -> Unit,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Insights & Progreso") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onIntent(InsightsIntent.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onIntent(InsightsIntent.OnCompareClick) }) {
                        Icon(Icons.Rounded.Compare, contentDescription = "Comparar fotos")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                WeightSummaryCards(state)

                InsightsSection(title = "Evolución de Peso") {
                    WeightChart(
                        weights = state.weightHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(top = 16.dp)
                    )
                }

                InsightsSection(title = "Consistencia de Hábitos") {
                    HabitStats(state)
                }
            }
        }
    }
}

@Composable
private fun InsightsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun WeightSummaryCards(
    state: InsightsState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Actual",
            value = "${state.currentWeight}kg",
            icon = Icons.Rounded.MonitorWeight,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Pérdida",
            value = String.format(java.util.Locale.getDefault(), "%.1fkg", state.totalWeightLost),
            icon = if (state.totalWeightLost >= 0) Icons.Rounded.TrendingDown else Icons.Rounded.North,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
    weights: List<com.emm.mybest.domain.models.WeightEntry>,
    modifier: Modifier = Modifier
) {
    if (weights.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                "Registra al menos 2 pesos para ver el gráfico",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
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
                endY = height
            )
        )

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4.dp.toPx())
        )

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun HabitStats(
    state: InsightsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { state.habitConsistency },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                )
                Text(
                    text = "${(state.habitConsistency * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Consistencia General", fontWeight = FontWeight.Bold)
                Text(
                    "Basado en todos tus registros",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        HorizontalStatRow(
            label = "Días de Ejercicio",
            count = state.exerciseDays,
            color = MaterialTheme.colorScheme.secondary
        )
        HorizontalStatRow(
            label = "Comida Saludable",
            count = state.healthyEatingDays,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun HorizontalStatRow(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = androidx.compose.foundation.shape.CircleShape,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "$count días",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

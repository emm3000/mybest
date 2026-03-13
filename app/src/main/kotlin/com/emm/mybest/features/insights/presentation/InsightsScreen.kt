package com.emm.mybest.features.insights.presentation

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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onCompareClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    val state by viewModel.state.collectAsState()

    HandleInsightsEffects(
        viewModel = viewModel,
        onCompareClick = onCompareClick,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            HTopBar(title = "Estadísticas de hábitos")
        },
    ) { padding ->
        InsightsBody(
            state = state,
            padding = padding,
            onCompareClick = { viewModel.onIntent(InsightsIntent.OnCompareClick) },
        )
    }
}

@Composable
private fun HandleInsightsEffects(
    viewModel: InsightsViewModel,
    onCompareClick: () -> Unit,
) {
    val currentOnCompareClick by androidx.compose.runtime.rememberUpdatedState(onCompareClick)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                InsightsEffect.NavigateBack -> Unit
                InsightsEffect.NavigateToCompare -> currentOnCompareClick()
            }
        }
    }
}

@Composable
private fun InsightsBody(
    state: InsightsState,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onCompareClick: () -> Unit,
) {
    val contentModifier = androidx.compose.ui.Modifier
        .padding(padding)
        .fillMaxSize()
        .padding(INSIGHTS_SCREEN_PADDING.dp)

    when {
        state.isLoading -> InsightsLoadingState(modifier = contentModifier)
        state.errorMessage != null -> {
            HAlert(
                title = "No se pudieron cargar las estadísticas",
                description = state.errorMessage,
                variant = AlertVariant.Destructive,
                modifier = contentModifier,
            )
        }
        state.weightHistory.isEmpty() && state.exerciseDays == 0 && state.healthyEatingDays == 0 -> {
            HEmptyState(
                title = "Sin datos para estadísticas",
                description = "Registra hábitos y evidencia para ver tu progreso en esta pantalla.",
                icon = Icons.Rounded.BarChart,
                modifier = contentModifier,
            )
        }
        else -> InsightsDataContent(
            state = state,
            onCompareClick = onCompareClick,
            modifier = contentModifier,
        )
    }
}

@Composable
private fun InsightsDataContent(
    state: InsightsState,
    onCompareClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_SECTION_SPACING.dp),
    ) {
        state.recommendation?.let { recommendation ->
            RecommendationSection(recommendation = recommendation)
        }

        WeightInsightsSection(state = state)

        InsightsComparePhotosSection(
            state = state,
            onCompareClick = onCompareClick,
        )

        InsightsSection(title = "Consistencia de Hábitos") {
            HabitStats(state)
        }
    }
}

@Composable
private fun RecommendationSection(
    recommendation: com.emm.mybest.domain.models.InsightsRecommendation,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    InsightsSection(
        title = "Recomendación de la semana",
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = recommendation.title,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = recommendation.description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Siguiente paso: ${recommendation.actionLabel}",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun InsightsLoadingState(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_SECTION_SPACING.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HSkeleton(
                modifier = androidx.compose.ui.Modifier
                    .weight(1f)
                    .height(96.dp),
                cornerRadius = INSIGHTS_SECTION_CORNER.dp,
            )
            HSkeleton(
                modifier = androidx.compose.ui.Modifier
                    .weight(1f)
                    .height(96.dp),
                cornerRadius = INSIGHTS_SECTION_CORNER.dp,
            )
        }
        HSkeleton(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .height(INSIGHTS_CHART_HEIGHT.dp),
            cornerRadius = INSIGHTS_SECTION_CORNER.dp,
        )
        HSkeleton(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .height(180.dp),
            cornerRadius = INSIGHTS_SECTION_CORNER.dp,
        )
    }
}

@Composable
internal fun InsightsSection(
    title: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = androidx.compose.ui.Modifier.padding(bottom = 12.dp),
        )
        HCard(
            variant = CardVariant.Filled,
            cornerRadius = INSIGHTS_SECTION_CORNER.dp,
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        ) {
            Box(modifier = androidx.compose.ui.Modifier.padding(INSIGHTS_SECTION_CONTENT_PADDING.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun HabitStats(
    state: InsightsState,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_STATS_GAP.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                androidx.compose.ui.Modifier.size(INSIGHTS_RING_SIZE.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { state.habitConsistency },
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                    trackColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                )
                Text(
                    text = "${(state.habitConsistency * 100).toInt()}%",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(androidx.compose.ui.Modifier.width(INSIGHTS_SECTION_SPACING.dp))
            Column {
                Text("Consistencia General", fontWeight = FontWeight.Bold)
                Text(
                    "Basado en todos tus registros",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                )
            }
        }

        HorizontalStatRow(
            label = "Días de Ejercicio",
            count = state.exerciseDays,
            color = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
        )
        HorizontalStatRow(
            label = "Comida Saludable",
            count = state.healthyEatingDays,
            color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
        )
    }
}

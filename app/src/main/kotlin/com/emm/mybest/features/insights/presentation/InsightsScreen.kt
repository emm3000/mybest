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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.models.InsightsRecommendation
import com.emm.mybest.domain.models.InsightsRecommendationAction
import com.emm.mybest.ui.components.AlertVariant
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HAlert
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HProgressRing
import com.emm.mybest.ui.components.HSkeleton
import com.emm.mybest.ui.components.StatChipVariant
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
    onRecommendationAction: (InsightsRecommendationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    HandleInsightsEffects(
        viewModel = viewModel,
        onCompareClick = onCompareClick,
        onRecommendationAction = onRecommendationAction,
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
            onRecommendationActionClick = { viewModel.onIntent(InsightsIntent.OnRecommendationActionClick) },
        )
    }
}

@Composable
private fun HandleInsightsEffects(
    viewModel: InsightsViewModel,
    onCompareClick: () -> Unit,
    onRecommendationAction: (InsightsRecommendationAction) -> Unit,
) {
    val currentOnCompareClick by androidx.compose.runtime.rememberUpdatedState(onCompareClick)
    val currentOnRecommendationAction by androidx.compose.runtime.rememberUpdatedState(onRecommendationAction)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                InsightsEffect.NavigateBack -> Unit
                InsightsEffect.NavigateToCompare -> currentOnCompareClick()
                is InsightsEffect.NavigateByRecommendation -> currentOnRecommendationAction(effect.action)
            }
        }
    }
}

@Composable
private fun InsightsBody(
    state: InsightsState,
    padding: androidx.compose.foundation.layout.PaddingValues,
    onCompareClick: () -> Unit,
    onRecommendationActionClick: () -> Unit,
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
            onRecommendationActionClick = onRecommendationActionClick,
            modifier = contentModifier,
        )
    }
}

@Composable
private fun InsightsDataContent(
    state: InsightsState,
    onCompareClick: () -> Unit,
    onRecommendationActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_SECTION_SPACING.dp),
    ) {
        Text(
            text = state.periodLabel,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
        )

        state.recommendation?.let { recommendation ->
            RecommendationSection(
                recommendation = recommendation,
                periodLabel = state.periodLabel,
                onActionClick = onRecommendationActionClick,
            )
        }

        WeightInsightsSection(state = state)

        InsightsComparePhotosSection(
            state = state,
            periodLabel = state.periodLabel,
            onCompareClick = onCompareClick,
        )

        InsightsSection(title = "Consistencia de Hábitos") {
            HabitStats(state)
        }
    }
}

@Composable
private fun RecommendationSection(
    recommendation: InsightsRecommendation,
    periodLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                text = periodLabel,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
            )
            Text(
                text = "Siguiente paso: ${recommendation.actionLabel}",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            )
            HButton(
                text = recommendation.actionLabel,
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun InsightsLoadingState(
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_STATS_GAP.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HProgressRing(
                progress = state.habitConsistency,
                size = INSIGHTS_RING_SIZE.dp,
                strokeWidth = 8.dp,
                showLabel = true,
            )
            Spacer(androidx.compose.ui.Modifier.width(INSIGHTS_SECTION_SPACING.dp))
            Column {
                Text("Consistencia General", fontWeight = FontWeight.Bold)
                Text(
                    state.periodLabel,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                )
            }
        }

        HorizontalStatRow(
            label = "Días de Ejercicio",
            count = state.exerciseDays,
            variant = StatChipVariant.Secondary,
        )
        HorizontalStatRow(
            label = "Comida Saludable",
            count = state.healthyEatingDays,
            variant = StatChipVariant.Tertiary,
        )
    }
}

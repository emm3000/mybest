package com.emm.mybest.features.insights.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import com.emm.mybest.ui.components.HSkeleton
import com.emm.mybest.ui.components.HTopBar
import java.util.Locale

private const val INSIGHTS_HORIZONTAL_PADDING = 16
private const val INSIGHTS_SECTION_SPACING = 16
private const val INSIGHTS_CORNER = 20
private const val INSIGHTS_HERO_PADDING = 24
private const val INSIGHTS_COMPARE_PADDING = 16

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    onCompareClick: () -> Unit,
    onRecommendationAction: (InsightsRecommendationAction) -> Unit,
    onHistoryClick: () -> Unit,
    onAddWeightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    HandleInsightsEffects(
        viewModel = viewModel,
        onCompareClick = onCompareClick,
        onRecommendationAction = onRecommendationAction,
        onHistoryClick = onHistoryClick,
        onAddWeightClick = onAddWeightClick,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            HTopBar(title = "Progreso")
        },
    ) { padding ->
        InsightsBody(
            state = state,
            padding = padding,
            onCompareClick = { viewModel.onIntent(InsightsIntent.OnCompareClick) },
            onRecommendationActionClick = { viewModel.onIntent(InsightsIntent.OnRecommendationActionClick) },
            onHistoryClick = { viewModel.onIntent(InsightsIntent.OnHistoryClick) },
            onAddWeightClick = { viewModel.onIntent(InsightsIntent.OnAddWeightClick) },
        )
    }
}

@Composable
private fun HandleInsightsEffects(
    viewModel: InsightsViewModel,
    onCompareClick: () -> Unit,
    onRecommendationAction: (InsightsRecommendationAction) -> Unit,
    onHistoryClick: () -> Unit,
    onAddWeightClick: () -> Unit,
) {
    val currentOnCompareClick by rememberUpdatedState(onCompareClick)
    val currentOnRecommendationAction by rememberUpdatedState(onRecommendationAction)
    val currentOnHistoryClick by rememberUpdatedState(onHistoryClick)
    val currentOnAddWeightClick by rememberUpdatedState(onAddWeightClick)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                InsightsEffect.NavigateBack -> Unit
                InsightsEffect.NavigateToCompare -> currentOnCompareClick()
                is InsightsEffect.NavigateByRecommendation -> currentOnRecommendationAction(effect.action)
                InsightsEffect.NavigateToHistory -> currentOnHistoryClick()
                InsightsEffect.NavigateToAddWeight -> currentOnAddWeightClick()
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
    onHistoryClick: () -> Unit,
    onAddWeightClick: () -> Unit,
) {
    val contentModifier = Modifier
        .padding(padding)
        .fillMaxSize()

    when {
        state.isLoading -> InsightsLoadingState(
            modifier = contentModifier.padding(horizontal = INSIGHTS_HORIZONTAL_PADDING.dp),
        )
        state.errorMessage != null -> {
            HAlert(
                title = "No se pudieron cargar las estadísticas",
                description = state.errorMessage,
                variant = AlertVariant.Destructive,
                modifier = contentModifier.padding(horizontal = INSIGHTS_HORIZONTAL_PADDING.dp),
            )
        }
        state.weightHistory.isEmpty() && state.photoCount == 0 -> {
            HEmptyState(
                title = "Sin datos aún",
                description = "Registra tu peso para empezar a ver tu progreso.",
                icon = Icons.Rounded.BarChart,
                modifier = contentModifier,
                action = {
                    HButton(
                        text = "Registrar primer peso",
                        onClick = onAddWeightClick,
                        variant = ButtonVariant.Default,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }
        else -> InsightsDataContent(
            state = state,
            onCompareClick = onCompareClick,
            onRecommendationActionClick = onRecommendationActionClick,
            onHistoryClick = onHistoryClick,
            modifier = contentModifier,
        )
    }
}

@Composable
private fun InsightsDataContent(
    state: InsightsState,
    onCompareClick: () -> Unit,
    onRecommendationActionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        // Period label — rendered ONCE at the top
        Text(
            text = state.periodLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = INSIGHTS_HORIZONTAL_PADDING.dp)
                .padding(top = 8.dp),
        )

        Column(
            modifier = Modifier.padding(
                horizontal = INSIGHTS_HORIZONTAL_PADDING.dp,
                vertical = INSIGHTS_SECTION_SPACING.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(INSIGHTS_SECTION_SPACING.dp),
        ) {
            // Hero Delta Card — top priority, 24dp inner padding, tappable to History
            HeroDeltaCard(
                state = state,
                onHistoryClick = onHistoryClick,
            )

            // Recommendation card — second priority
            state.recommendation?.let { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    onActionClick = onRecommendationActionClick,
                )
            }

            // Compare photos — only when canComparePhotos
            if (state.canComparePhotos) {
                ComparePhotosCard(
                    photoCount = state.photoCount,
                    onCompareClick = onCompareClick,
                )
            }
        }
    }
}

@Composable
private fun HeroDeltaCard(
    state: InsightsState,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HCard(
        variant = CardVariant.Outlined,
        cornerRadius = INSIGHTS_CORNER.dp,
        onClick = onHistoryClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(INSIGHTS_HERO_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (state.weightHistory.isEmpty()) {
                Text(
                    text = "Sin registros aún",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                HeroDeltaCardData(state = state)
            }
        }
    }
}

@Composable
private fun HeroDeltaCardData(
    state: InsightsState,
) {
    val cs = MaterialTheme.colorScheme
    val deltaColor = when {
        state.totalWeightLost > 0f -> cs.primary
        state.totalWeightLost < 0f -> cs.error
        else -> cs.onSurface
    }
    val deltaSign = when {
        state.totalWeightLost > 0f -> "-"
        state.totalWeightLost < 0f -> "+"
        else -> ""
    }
    val deltaAbs = formatWeight(kotlin.math.abs(state.totalWeightLost))

    Text(
        text = "$deltaSign$deltaAbs",
        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
        color = deltaColor,
    )

    val arrow = if (state.totalWeightLost >= 0f) "↓" else "↑"
    Text(
        text = "$arrow desde ${formatWeight(state.initialWeight)} → ${formatWeight(state.currentWeight)}",
        style = MaterialTheme.typography.bodySmall,
        color = cs.onSurfaceVariant,
    )
}

@Composable
private fun RecommendationCard(
    recommendation: InsightsRecommendation,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderStrokeWidth = 2.dp.value

    HCard(
        variant = CardVariant.Outlined,
        cornerRadius = INSIGHTS_CORNER.dp,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = primaryColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = borderStrokeWidth * density,
                )
            },
    ) {
        Column(
            modifier = Modifier.padding(INSIGHTS_HERO_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = primaryColor,
                )
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (recommendation.action != InsightsRecommendationAction.KEEP_ROUTINE) {
                HButton(
                    text = recommendation.actionLabel,
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Default,
                )
            }
        }
    }
}

@Composable
private fun ComparePhotosCard(
    photoCount: Int,
    onCompareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HCard(
        variant = CardVariant.Outlined,
        cornerRadius = INSIGHTS_CORNER.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(INSIGHTS_COMPARE_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Compare,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Comparar fotos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "$photoCount fotos disponibles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HButton(
                text = "Abrir comparador →",
                onClick = onCompareClick,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.Outline,
            )
        }
    }
}

@Composable
private fun InsightsLoadingState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(INSIGHTS_SECTION_SPACING.dp),
    ) {
        // Hero skeleton
        HSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            cornerRadius = INSIGHTS_CORNER.dp,
        )
        // Recommendation skeleton
        HSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            cornerRadius = INSIGHTS_CORNER.dp,
        )
        // Compare skeleton
        HSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            cornerRadius = INSIGHTS_CORNER.dp,
        )
    }
}

private fun formatWeight(weight: Float): String =
    String.format(Locale.getDefault(), "%.1f kg", weight)

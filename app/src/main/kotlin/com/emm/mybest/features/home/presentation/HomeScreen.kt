package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.core.datetime.currentDate
import com.emm.mybest.core.navigation.Screen
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HSkeleton
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.theme.MyBestTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentOnNavigate by androidx.compose.runtime.rememberUpdatedState(onNavigate)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.Navigate -> currentOnNavigate(effect.route)
                is HomeEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is HomeEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    HomeScreenContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().consumeWindowInsets(WindowInsets.safeContent),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { HSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HomeHeader(modifier = Modifier.fillMaxWidth())
            }

            homeHabitsSection(state = state, onIntent = onIntent)

            homePrimaryActionsSection(state = state, onIntent = onIntent)

            item {
                SummaryCard(
                    state = state,
                    onClick = { onIntent(HomeIntent.OnViewInsightsClick) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun HomeHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Mi Mejor Versión",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Hoy es ${currentDate()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SummaryCard(
    state: HomeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HCard(
        modifier = modifier.height(160.dp),
        onClick = onClick,
        variant = CardVariant.Filled,
        cornerRadius = 28.dp,
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterStart),
            ) {
                if (state.isLoading) {
                    SummaryCardLoadingState()
                } else {
                    Text(
                        text = "Tu Progreso",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = progressHeadline(state.totalWeightLost),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toca para ver tus estadísticas",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Icon(
                Icons.Rounded.BarChart,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(24.dp)
                    .size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun SummaryCardLoadingState() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HSkeleton(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp),
            cornerRadius = 8.dp,
        )
        HSkeleton(
            modifier = Modifier
                .width(180.dp)
                .height(36.dp),
            cornerRadius = 12.dp,
        )
        HSkeleton(
            modifier = Modifier
                .width(150.dp)
                .height(14.dp),
            cornerRadius = 8.dp,
        )
    }
}

private fun progressHeadline(totalWeightLost: Float): String {
    if (totalWeightLost <= 0f) {
        return "¡Vas muy bien!"
    }

    val formattedWeight = String.format(
        java.util.Locale.getDefault(),
        "%.1f",
        totalWeightLost,
    )
    return "¡Has bajado $formattedWeight kg!"
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HCard(
        onClick = onClick,
        cornerRadius = 24.dp,
        variant = CardVariant.Filled,
        containerColor = containerColor,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.5f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MyBestTheme {
        HomeScreenContent(
            state = HomeState(
                lastWeight = 75.5f,
                totalPhotos = 12,
                isLoading = false,
            ),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

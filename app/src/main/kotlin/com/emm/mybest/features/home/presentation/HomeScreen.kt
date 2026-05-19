package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HProgressBar
import com.emm.mybest.ui.components.HSeparator
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.HTopBar
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.ui.theme.StarlinkTextStyles
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

data class HomeCallbacks(
    val onWeightClick: () -> Unit,
    val onPhotoClick: () -> Unit,
    val onMealPlanClick: () -> Unit,
    val onExercisePlanClick: () -> Unit,
    val onSettingsClick: () -> Unit,
    val onHistoryClick: () -> Unit,
)

@Composable
fun HomeScreen(
    callbacks: HomeCallbacks,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    HomeScreenContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::onIntent,
        callbacks = callbacks,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    callbacks: HomeCallbacks,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { HSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            HTopBar(
                title = formatTopbarDate(state.today, state.dayOfWeek),
                actions = {
                    IconButton(onClick = callbacks.onSettingsClick) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Ajustes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HomeDayHeroCard(
                    state = state,
                    onHistoryClick = callbacks.onHistoryClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                HomePrimaryCtaRow(
                    onWeightClick = callbacks.onWeightClick,
                    onPhotoClick = callbacks.onPhotoClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                HomeMealSection(
                    state = state,
                    onIntent = onIntent,
                    onMealPlanClick = callbacks.onMealPlanClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                HomeExerciseSection(
                    state = state,
                    onIntent = onIntent,
                    onExercisePlanClick = callbacks.onExercisePlanClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── Hero card ────────────────────────────────────────────────────────────────

@Composable
private fun HomeDayHeroCard(
    state: HomeState,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HCard(
        modifier = modifier.clickable(onClick = onHistoryClick),
        variant = CardVariant.Outlined,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "${state.completedCount}/${state.totalCount}",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CUMPLIDO",
                style = StarlinkTextStyles.sectionLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            HProgressBar(
                progress = state.completionRatio,
                height = 4.dp,
                indicatorColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Primary CTA row ──────────────────────────────────────────────────────────

@Composable
private fun HomePrimaryCtaRow(
    onWeightClick: () -> Unit,
    onPhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HButton(
            text = "PESO",
            onClick = onWeightClick,
            variant = ButtonVariant.Default,
            leadingIcon = Icons.Rounded.MonitorWeight,
            modifier = Modifier.weight(1f),
        )
        HButton(
            text = "FOTO",
            onClick = onPhotoClick,
            variant = ButtonVariant.Default,
            leadingIcon = Icons.Rounded.PhotoCamera,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Meals section ────────────────────────────────────────────────────────────

@Composable
private fun HomeMealSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onMealPlanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completedMeals = state.mealRows.count { it.done }
    val totalMeals = state.mealRows.size
    HCard(modifier = modifier, variant = CardVariant.Outlined) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
            HomeSectionHeader(
                label = "COMIDAS",
                count = "$completedMeals/$totalMeals",
                icon = {
                    Icon(
                        Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = onMealPlanClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
            HSeparator()
            val haptic = LocalHapticFeedback.current
            state.mealRows.forEach { row ->
                HomePlanRow(
                    label = row.type.labelEs(),
                    description = row.description,
                    done = row.done,
                    onCheckedChange = { done ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onIntent(HomeIntent.ToggleMeal(row.type, done))
                    },
                )
            }
        }
    }
}

// ── Exercise section ─────────────────────────────────────────────────────────

@Composable
private fun HomeExerciseSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onExercisePlanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completedExercise = if (state.exerciseDone) 1 else 0
    HCard(modifier = modifier, variant = CardVariant.Outlined) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
            HomeSectionHeader(
                label = "EJERCICIO",
                count = "$completedExercise/1",
                icon = {
                    Icon(
                        Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = onExercisePlanClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
            HSeparator()
            val haptic = LocalHapticFeedback.current
            HomePlanRow(
                label = "RUTINA",
                description = state.exerciseRoutine,
                done = state.exerciseDone,
                onCheckedChange = { done ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onIntent(HomeIntent.ToggleExercise(done))
                },
                emptyPlaceholder = "Sin rutina",
            )
        }
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun HomeSectionHeader(
    label: String,
    count: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon()
            Text(
                text = label,
                style = StarlinkTextStyles.sectionLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = count,
            style = StarlinkTextStyles.chipLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HomePlanRow(
    label: String,
    description: String,
    done: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    emptyPlaceholder: String = "Sin plan",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!done) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = done, onCheckedChange = onCheckedChange)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = StarlinkTextStyles.chipLabel,
                color = if (done) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            val hasContent = description.isNotEmpty()
            Text(
                text = if (hasContent) description else emptyPlaceholder,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = if (done && hasContent) TextDecoration.LineThrough else TextDecoration.None,
            )
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MyBestTheme {
        HomeScreenContent(
            state = HomeState(
                isLoading = false,
                mealRows = MealType.entries.map { MealRow(it, "", false) },
                completedCount = 2,
                totalCount = 5,
                completionRatio = 0.4f,
            ),
            onIntent = {},
            callbacks = HomeCallbacks(
                onWeightClick = {},
                onPhotoClick = {},
                onMealPlanClick = {},
                onExercisePlanClick = {},
                onSettingsClick = {},
                onHistoryClick = {},
            ),
        )
    }
}

package com.emm.mybest.features.home.presentation

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
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.domain.models.MealType
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.CardVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HProgressBar
import com.emm.mybest.ui.components.HTopBar
import com.emm.mybest.ui.theme.MyBestTheme
import com.emm.mybest.ui.theme.StarlinkTextStyles
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onWeightClick: () -> Unit,
    onPhotoClick: () -> Unit,
    onMealPlanClick: () -> Unit,
    onExercisePlanClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    HomeScreenContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::handle,
        onWeightClick = onWeightClick,
        onPhotoClick = onPhotoClick,
        onMealPlanClick = onMealPlanClick,
        onExercisePlanClick = onExercisePlanClick,
    )
}

@Composable
internal fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onWeightClick: () -> Unit,
    onPhotoClick: () -> Unit,
    onMealPlanClick: () -> Unit,
    onExercisePlanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HTopBar(title = "Hoy") },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HomeDayHeroCard(state = state, modifier = Modifier.fillMaxWidth()) }
            item { HomeMealSection(state = state, onIntent = onIntent, modifier = Modifier.fillMaxWidth()) }
            item { HomeExerciseSection(state = state, onIntent = onIntent, modifier = Modifier.fillMaxWidth()) }
            item {
                HomeEditorsRow(
                    onMealPlanClick = onMealPlanClick,
                    onExercisePlanClick = onExercisePlanClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                HomePrimaryCtaSection(
                    onWeightClick = onWeightClick,
                    onPhotoClick = onPhotoClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── Hero card ────────────────────────────────────────────────────────────────

@Composable
private fun HomeDayHeroCard(state: HomeState, modifier: Modifier = Modifier) {
    HCard(modifier = modifier, variant = CardVariant.Outlined) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "${state.dayOfWeek.longEs().uppercase()} · ${state.today.formatShortMonthDay()}",
                style = StarlinkTextStyles.sectionLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${state.completedCount}/${state.totalCount}",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CUMPLIDO",
                style = StarlinkTextStyles.sectionLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            HProgressBar(
                progress = state.completionRatio,
                height = 2.dp,
                indicatorColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Meals section ────────────────────────────────────────────────────────────

@Composable
private fun HomeMealSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HCard(modifier = modifier, variant = CardVariant.Outlined) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
            HomeSectionHeader(
                label = "COMIDAS",
                icon = {
                    Icon(
                        Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            state.mealRows.forEach { row ->
                HomePlanRow(
                    label = row.type.labelEs(),
                    description = row.description,
                    done = row.done,
                    onCheckedChange = { done -> onIntent(HomeIntent.ToggleMeal(row.type, done)) },
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
    modifier: Modifier = Modifier,
) {
    HCard(modifier = modifier, variant = CardVariant.Outlined) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
            HomeSectionHeader(
                label = "EJERCICIO",
                icon = {
                    Icon(
                        Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            Spacer(modifier = Modifier.height(8.dp))
            HomePlanRow(
                label = "EJERCICIO",
                description = state.exerciseRoutine,
                done = state.exerciseDone,
                onCheckedChange = { done -> onIntent(HomeIntent.ToggleExercise(done)) },
                emptyPlaceholder = "Sin rutina",
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun HomeSectionHeader(
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = done, onCheckedChange = onCheckedChange)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = StarlinkTextStyles.chipLabel,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val hasContent = description.isNotEmpty()
            Text(
                text = if (hasContent) description else emptyPlaceholder,
                style = MaterialTheme.typography.bodySmall,
                color = if (hasContent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Editor CTAs ──────────────────────────────────────────────────────────────

@Composable
private fun HomeEditorsRow(
    onMealPlanClick: () -> Unit,
    onExercisePlanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HButton(
            text = "EDITAR DIETA",
            onClick = onMealPlanClick,
            variant = ButtonVariant.Outline,
            leadingIcon = Icons.Rounded.Restaurant,
            modifier = Modifier.weight(1f),
        )
        HButton(
            text = "EDITAR RUTINA",
            onClick = onExercisePlanClick,
            variant = ButtonVariant.Outline,
            leadingIcon = Icons.Rounded.FitnessCenter,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Primary CTAs ─────────────────────────────────────────────────────────────

@Composable
private fun HomePrimaryCtaSection(
    onWeightClick: () -> Unit,
    onPhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HButton(text = "REGISTRAR PESO", onClick = onWeightClick, modifier = Modifier.fillMaxWidth())
        HButton(
            text = "TOMAR FOTO",
            onClick = onPhotoClick,
            variant = ButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
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
            onWeightClick = {},
            onPhotoClick = {},
            onMealPlanClick = {},
            onExercisePlanClick = {},
        )
    }
}

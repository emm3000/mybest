package com.emm.mybest.features.home.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.components.HCard
import com.emm.mybest.ui.components.HEmptyState
import com.emm.mybest.ui.components.HSkeleton
import com.emm.mybest.ui.components.HStatChip
import com.emm.mybest.ui.components.HabitCard

private const val HOME_HABIT_SKELETON_COUNT = 3
private const val HOME_HABIT_SKELETON_TITLE_WIDTH = 0.48f
private const val HOME_HABIT_SKELETON_SUBTITLE_WIDTH = 0.3f

internal fun LazyListScope.homeHabitsSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) {
    item {
        HomeHabitsSection(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HomeHabitsSection(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Hábitos de hoy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        HomeDailyProgressSummary(state = state)

        AnimatedContent(
            targetState = state.homeHabitsContentKind(),
            label = "HomeHabitsContent",
        ) { contentKind ->
            when (contentKind) {
                HomeHabitsContentKind.Loading -> HomeHabitsLoadingContent()
                HomeHabitsContentKind.Empty -> {
                    HEmptyState(
                        title = "Sin hábitos para hoy",
                        description = "Crea un nuevo hábito para empezar a dar seguimiento a tu progreso.",
                        icon = Icons.Rounded.CheckCircle,
                        action = {
                            HButton(
                                text = "Añadir Hábito",
                                onClick = { onIntent(HomeIntent.OnAddHabitClick) },
                            )
                        },
                    )
                }

                HomeHabitsContentKind.Content -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.dailyHabits.forEach { habitWithRecord ->
                            HabitCard(
                                habit = habitWithRecord.habit,
                                record = habitWithRecord.record,
                                onToggle = { onIntent(HomeIntent.ToggleHabit(habitWithRecord)) },
                                onClick = { onIntent(HomeIntent.OnEditHabitClick(habitWithRecord.habit.id)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDailyProgressSummary(
    state: HomeState,
    modifier: Modifier = Modifier,
) {
    val totalHabits = state.dailyHabits.size
    val completedHabits = state.completedHabitsCount
    val pendingHabits = state.pendingHabitsCount

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HStatChip(
            label = "Pendientes",
            value = pendingHabits.toString(),
            modifier = Modifier.weight(1f),
        )
        HStatChip(
            label = "Completados",
            value = completedHabits.toString(),
            modifier = Modifier.weight(1f),
        )
        HStatChip(
            label = "Total",
            value = totalHabits.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HomeHabitsLoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(HOME_HABIT_SKELETON_COUNT) {
            HomeHabitSkeletonCard(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun HomeHabitSkeletonCard(modifier: Modifier = Modifier) {
    HCard(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HSkeleton(
                modifier = Modifier.size(40.dp),
                cornerRadius = 999.dp,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HSkeleton(
                    modifier = Modifier
                        .fillMaxWidth(HOME_HABIT_SKELETON_TITLE_WIDTH)
                        .height(18.dp),
                    cornerRadius = 8.dp,
                )
                HSkeleton(
                    modifier = Modifier
                        .fillMaxWidth(HOME_HABIT_SKELETON_SUBTITLE_WIDTH)
                        .height(14.dp),
                    cornerRadius = 8.dp,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            HSkeleton(
                modifier = Modifier.size(24.dp),
                cornerRadius = 999.dp,
            )
        }
    }
}

private fun HomeState.homeHabitsContentKind(): HomeHabitsContentKind {
    if (isLoading) return HomeHabitsContentKind.Loading
    if (dailyHabits.isEmpty()) return HomeHabitsContentKind.Empty
    return HomeHabitsContentKind.Content
}

private sealed interface HomeHabitsContentKind {
    data object Loading : HomeHabitsContentKind
    data object Empty : HomeHabitsContentKind
    data object Content : HomeHabitsContentKind
}

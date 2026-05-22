package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.mybest.R
import com.emm.mybest.domain.models.DailySlot
import com.emm.mybest.domain.models.DailySlotTimes
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.atelier.CompletionCheck
import com.emm.mybest.ui.components.atelier.DisplayNumber
import com.emm.mybest.ui.components.atelier.DisplayNumberStyle
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInkTertiary
import com.emm.mybest.ui.theme.AtelierTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

private val HERO_NUMBER_FONT_SIZE = 128.sp
internal val HERO_SLASH_FONT_SIZE = 56.sp
private val PLAN_ROW_MIN_HEIGHT = 60.dp
private val PLAN_ROW_PADDING_HORIZONTAL = 28.dp
private val PLAN_ROW_PADDING_VERTICAL = 14.dp
private val PLAN_ROW_SPACING = 16.dp
private val SLOT_LABEL_WIDTH = 84.dp
private val QUICK_ACTION_HEIGHT = 80.dp
private val QUICK_ACTION_CELL_SPACER = 4.dp
private const val PERCENT_FACTOR = 100

data class HomeCallbacks(
    val onWeightClick: () -> Unit,
    val onPhotoClick: () -> Unit,
    val onMealPlanClick: () -> Unit,
    val onExercisePlanClick: () -> Unit,
    val onSettingsClick: () -> Unit,
    val onHistoryClick: () -> Unit,
)

data class QuickActionCellContent(
    val label: String,
    val number: String,
    val unit: String? = null,
    val caption: String? = null,
    val isPlaceholder: Boolean = false,
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
    ) { paddingValues ->
        HomeLazyContent(state, onIntent, callbacks, paddingValues)
    }
    state.editingMeal?.let { draft ->
        EditMealSheet(draft = draft, onIntent = onIntent)
    }
}

@Composable
private fun HomeLazyContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    callbacks: HomeCallbacks,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        item { HomeHeaderRow(state) }
        item { Hairline() }
        item { HomeHero(state) }
        item { Hairline() }
        itemsIndexed(state.planRows) { index, row ->
            val onEditRequest: () -> Unit = if (row.slot == DailySlot.EXERCISE) {
                {}
            } else {
                { onIntent(HomeIntent.StartEditMeal(row.slot)) }
            }
            HomePlanRow(
                label = row.slot.labelEs(),
                time = row.time.formatHHmm(),
                description = row.description,
                done = row.done,
                onToggle = { onIntent(HomeIntent.ToggleSlot(row.slot, !row.done)) },
                onEditRequest = onEditRequest,
            )
            if (index < state.planRows.lastIndex) {
                Hairline(inset = PLAN_ROW_PADDING_HORIZONTAL)
            }
        }
        item { Hairline() }
        item { HomeQuickActionsRow(state, callbacks) }
    }
}

@Composable
private fun HomeHeaderRow(state: HomeState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PLAN_ROW_PADDING_HORIZONTAL, vertical = PLAN_ROW_PADDING_VERTICAL),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(
            text = formatHomeHeaderDate(state.today, state.dayOfWeek),
            style = MicroLabelStyle(tone = MicroLabelTone.Default),
        )
        MicroLabel(
            text = stringResource(R.string.home_week_format, state.weekNumber).uppercase(),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

@Composable
private fun HomeHeroMetrics(state: HomeState) {
    val percentText = "${(state.completionRatio * PERCENT_FACTOR).toInt()}%"
    Column(horizontalAlignment = Alignment.End) {
        MicroLabel(text = percentText, style = MicroLabelStyle(tone = MicroLabelTone.Default))
        MicroLabel(
            text = stringResource(R.string.home_streak_days_format, state.streakDays).uppercase(),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        MicroLabel(
            text = stringResource(R.string.home_completed_today).uppercase(),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

@Composable
private fun HomeHero(state: HomeState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PLAN_ROW_PADDING_HORIZONTAL, vertical = PLAN_ROW_PADDING_VERTICAL),
        verticalAlignment = Alignment.Bottom,
    ) {
        DisplayNumber(
            text = state.completedCount.toString(),
            style = DisplayNumberStyle(fontSize = HERO_NUMBER_FONT_SIZE, italic = true),
        )
        DisplayNumber(
            text = "/${state.totalCount}",
            style = DisplayNumberStyle(fontSize = HERO_SLASH_FONT_SIZE, color = AtelierInkTertiary),
        )
        Spacer(modifier = Modifier.weight(1f))
        HomeHeroMetrics(state)
    }
}

@Composable
private fun HomePlanRow(
    label: String,
    time: String,
    description: String,
    done: Boolean,
    onToggle: () -> Unit,
    onEditRequest: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val hasDescription = description.isNotBlank()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = PLAN_ROW_MIN_HEIGHT)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEditRequest()
                },
            )
            .padding(horizontal = PLAN_ROW_PADDING_HORIZONTAL, vertical = PLAN_ROW_PADDING_VERTICAL),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PLAN_ROW_SPACING),
    ) {
        Column(modifier = Modifier.width(SLOT_LABEL_WIDTH)) {
            MicroLabel(
                text = label,
                style = MicroLabelStyle(tone = if (done) MicroLabelTone.Done else MicroLabelTone.Default),
            )
            MicroLabel(text = time, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        }
        Text(
            text = if (hasDescription) description else "—",
            style = MaterialTheme.typography.bodyMedium,
            color = if (done) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (done && hasDescription) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )
        CompletionCheck(done = done)
    }
}

@Composable
private fun HomeQuickActionsRow(state: HomeState, callbacks: HomeCallbacks) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(QUICK_ACTION_HEIGHT),
    ) {
        HomeQuickActionCell(
            content = weightCellContent(state),
            onClick = callbacks.onWeightClick,
            modifier = Modifier.weight(1f),
        )
        QuickActionDivider()
        HomeQuickActionCell(
            content = photoCellContent(state),
            onClick = callbacks.onPhotoClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HomeQuickActionCell(
    content: QuickActionCellContent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = PLAN_ROW_PADDING_HORIZONTAL, vertical = PLAN_ROW_PADDING_VERTICAL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MicroLabel(text = content.label, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        Spacer(modifier = Modifier.height(QUICK_ACTION_CELL_SPACER))
        QuickActionNumberRow(content.number, content.unit, content.isPlaceholder)
        if (content.caption != null) {
            MicroLabel(text = content.caption, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AtelierTheme {
        HomeScreenContent(
            state = HomeState(
                isLoading = false,
                planRows = DailySlot.entries.map { slot ->
                    PlanRow(
                        slot = slot,
                        time = DailySlotTimes.DEFAULT_TIMES.getValue(slot),
                        description = "",
                        done = false,
                    )
                },
                completedCount = 2,
                totalCount = 5,
                completionRatio = 0.4f,
                weekNumber = 20,
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

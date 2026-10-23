package com.emm.mybest.features.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.domain.usecase.home.HomeDestination
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierInkSecondary
import com.emm.mybest.ui.theme.AtelierTheme
import org.koin.androidx.compose.koinViewModel

private enum class HomeContentType { Header, Headline, Hairline, QuickActions, Checks, Week, Shortcuts }

private val ROW_PADDING_HORIZONTAL = 28.dp
private val ROW_PADDING_VERTICAL = 14.dp
private val QUICK_ACTION_HEIGHT_MIN = 80.dp
private val QUICK_ACTION_CELL_SPACER = 4.dp
private val SETTINGS_ICON_SIZE = 18.dp

data class HomeCallbacks(
    val onWeightClick: () -> Unit,
    val onPhotoClick: () -> Unit,
    val onSettingsClick: () -> Unit,
    val onHistoryClick: () -> Unit,
    val onShortcutClick: (HomeShortcut) -> Unit,
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
    slots: HomeSlots,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreenEffects(viewModel, callbacks)
    if (state.destination == HomeDestination.DAILY_CHECKS) {
        HomeScreenContent(
            modifier = modifier,
            state = state,
            callbacks = callbacks,
            onIntent = viewModel::onIntent,
            bottomBar = slots.bottomBar,
        )
    } else {
        HomeRoutedScreen(
            destination = state.destination,
            onShowDailyChecks = { viewModel.onIntent(HomeIntent.ShowDailyChecks) },
            slots = slots,
            modifier = modifier,
        )
    }
}

@Composable
private fun HomeScreenEffects(viewModel: HomeViewModel, callbacks: HomeCallbacks) {
    LifecycleResumeEffect(viewModel) {
        viewModel.onIntent(HomeIntent.Resume)
        onPauseOrDispose {}
    }
    val currentCallbacks by rememberUpdatedState(callbacks)
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToSettings -> currentCallbacks.onSettingsClick()
            }
        }
    }
}

@Composable
internal fun HomeScreenContent(
    state: HomeState,
    callbacks: HomeCallbacks,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = bottomBar,
    ) { paddingValues ->
        HomeLazyContent(state, callbacks, onIntent, paddingValues)
    }
}

@Composable
private fun HomeLazyContent(
    state: HomeState,
    callbacks: HomeCallbacks,
    onIntent: (HomeIntent) -> Unit,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        item(contentType = HomeContentType.Header) { HomeHeaderRow(state, callbacks.onSettingsClick) }
        item(contentType = HomeContentType.Headline) { HomeHeadline() }
        item(contentType = HomeContentType.Hairline) { Hairline() }
        item(contentType = HomeContentType.QuickActions) { HomeQuickActionsRow(state, callbacks) }
        item(contentType = HomeContentType.Hairline) { Hairline() }
        item(contentType = HomeContentType.Checks) {
            HomeChecksSection(
                model = HomeChecksModel(state.checks, state.keptChecks, state.totalChecks),
                onToggle = { onIntent(HomeIntent.ToggleDailyCheck(it)) },
            )
        }
        item(contentType = HomeContentType.Week) {
            HomeWeekSection(
                checks = state.checks,
                onDefineTarget = { onIntent(HomeIntent.DefineWeeklyTarget) },
            )
        }
        item(contentType = HomeContentType.Shortcuts) {
            HomeShortcutsSection(shortcuts = state.shortcuts, onShortcutClick = callbacks.onShortcutClick)
        }
    }
}

@Composable
private fun HomeHeaderRow(state: HomeState, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ROW_PADDING_HORIZONTAL, vertical = ROW_PADDING_VERTICAL),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicroLabel(
            text = formatHomeHeaderDate(state.today, state.dayOfWeek),
            style = MicroLabelStyle(tone = MicroLabelTone.Default),
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.nav_settings),
                modifier = Modifier.size(SETTINGS_ICON_SIZE),
                tint = AtelierInkSecondary,
            )
        }
    }
}

@Composable
private fun HomeHeadline() {
    Text(
        text = stringResource(R.string.home_headline),
        modifier = Modifier.padding(horizontal = ROW_PADDING_HORIZONTAL, vertical = ROW_PADDING_VERTICAL),
        style = MaterialTheme.typography.headlineLarge,
    )
}

@Composable
private fun HomeQuickActionsRow(state: HomeState, callbacks: HomeCallbacks) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = QUICK_ACTION_HEIGHT_MIN),
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
            .padding(horizontal = ROW_PADDING_HORIZONTAL, vertical = ROW_PADDING_VERTICAL),
        horizontalAlignment = Alignment.Start,
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
                checks = previewDailyChecks(),
                shortcuts = HomeShortcut.entries,
            ),
            callbacks = HomeCallbacks(
                onWeightClick = {},
                onPhotoClick = {},
                onSettingsClick = {},
                onHistoryClick = {},
                onShortcutClick = {},
            ),
            onIntent = {},
        )
    }
}

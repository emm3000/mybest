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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.atelier.Hairline
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierTheme
import org.koin.androidx.compose.koinViewModel

private enum class HomeContentType { Header, Hairline, QuickActions }

private val ROW_PADDING_HORIZONTAL = 28.dp
private val ROW_PADDING_VERTICAL = 14.dp
private val QUICK_ACTION_HEIGHT_MIN = 80.dp
private val QUICK_ACTION_CELL_SPACER = 4.dp

data class HomeCallbacks(
    val onWeightClick: () -> Unit,
    val onPhotoClick: () -> Unit,
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
    bottomBar: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreenContent(
        modifier = modifier,
        state = state,
        callbacks = callbacks,
        bottomBar = bottomBar,
    )
}

@Composable
internal fun HomeScreenContent(
    state: HomeState,
    callbacks: HomeCallbacks,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = bottomBar,
    ) { paddingValues ->
        HomeLazyContent(state, callbacks, paddingValues)
    }
}

@Composable
private fun HomeLazyContent(
    state: HomeState,
    callbacks: HomeCallbacks,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        item(contentType = HomeContentType.Header) { HomeHeaderRow(state) }
        item(contentType = HomeContentType.Hairline) { Hairline() }
        item(contentType = HomeContentType.QuickActions) { HomeQuickActionsRow(state, callbacks) }
    }
}

@Composable
private fun HomeHeaderRow(state: HomeState) {
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
        MicroLabel(
            text = stringResource(R.string.home_week_format, state.weekNumber).uppercase(),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
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
                weekNumber = 20,
            ),
            callbacks = HomeCallbacks(
                onWeightClick = {},
                onPhotoClick = {},
                onSettingsClick = {},
                onHistoryClick = {},
            ),
        )
    }
}

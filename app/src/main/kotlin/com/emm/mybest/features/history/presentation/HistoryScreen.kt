package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.emm.mybest.R
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierBackground
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

private const val PAGER_COUNT = Int.MAX_VALUE
private const val PAGER_INITIAL = PAGER_COUNT / 2

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onSeePhotosClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    val selectedDate = state.selectedDate
    if (selectedDate != null) {
        HBottomSheet(
            onDismissRequest = { viewModel.onIntent(HistoryIntent.OnDateDismiss) },
            containerColor = AtelierBackground,
        ) {
            DayDetailContent(
                date = selectedDate,
                summary = state.monthlyData[selectedDate],
                today = today,
                onClose = { viewModel.onIntent(HistoryIntent.OnDateDismiss) },
                onDeleteWeight = { viewModel.onIntent(HistoryIntent.OnDeleteWeight(selectedDate)) },
                onDeletePhoto = { viewModel.onIntent(HistoryIntent.OnDeletePhoto(it)) },
                onSeePhotosClick = onSeePhotosClick,
            )
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = AtelierBackground,
        topBar = {
            AtelierAppBar(
                title = stringResource(R.string.history_title),
                actions = {
                    MicroLabel(
                        text = stringResource(R.string.history_nav_label),
                        style = MicroLabelStyle(tone = MicroLabelTone.Dim),
                        modifier = Modifier.padding(end = HISTORY_GUT),
                    )
                },
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        HistoryPagerContent(
            state = state,
            today = today,
            onMonthChange = { viewModel.onIntent(HistoryIntent.OnMonthChange(it)) },
            onDateClick = { viewModel.onIntent(HistoryIntent.OnDateSelected(it)) },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun HistoryPagerContent(
    state: HistoryState,
    today: LocalDate,
    onMonthChange: (YearMonthValue) -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> HistoryLoadingContent(modifier = modifier)
        state.errorMessage != null -> HistoryErrorContent(
            message = state.errorMessage,
            modifier = modifier,
        )
        else -> HistoryPagedContent(
            state = state,
            today = today,
            onMonthChange = onMonthChange,
            onDateClick = onDateClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun HistoryPagedContent(
    state: HistoryState,
    today: LocalDate,
    onMonthChange: (YearMonthValue) -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val referenceMonth = remember { YearMonthValue.now() }
    val pagerState = rememberPagerState(
        initialPage = PAGER_INITIAL,
        pageCount = { PAGER_COUNT },
    )

    LaunchedEffect(pagerState.currentPage) {
        val offset = pagerState.currentPage - PAGER_INITIAL
        onMonthChange(referenceMonth.plusMonths(offset))
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) {
        HistoryMonthPage(
            state = state,
            today = today,
            onDateClick = onDateClick,
        )
    }
}

@Composable
private fun HistoryLoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        MicroLabel(
            text = stringResource(R.string.history_loading),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
    }
}

@Composable
private fun HistoryErrorContent(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = HISTORY_GUT),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MicroLabel(
            text = stringResource(R.string.history_error_label),
            style = MicroLabelStyle(tone = MicroLabelTone.Dim),
        )
        MicroLabel(text = message, style = MicroLabelStyle(tone = MicroLabelTone.Dim))
    }
}

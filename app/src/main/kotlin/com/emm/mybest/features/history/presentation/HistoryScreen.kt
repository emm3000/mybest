package com.emm.mybest.features.history.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HBottomSheet
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.atelier.MicroLabel
import com.emm.mybest.ui.components.atelier.MicroLabelStyle
import com.emm.mybest.ui.components.atelier.MicroLabelTone
import com.emm.mybest.ui.theme.AtelierBackground
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

private const val PAGER_COUNT = Int.MAX_VALUE
private const val PAGER_INITIAL = PAGER_COUNT / 2

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onSeePhotosClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = state.today
    val snackbarHostState = remember { SnackbarHostState() }
    HistoryEffectsHandler(effects = viewModel.effect, snackbarHostState = snackbarHostState)
    state.noteEditor?.let { NoteEditorOverlay(editor = it, onIntent = viewModel::onIntent) }

    state.dayDetail?.let { detail ->
        HBottomSheet(
            onDismissRequest = { viewModel.onIntent(HistoryIntent.OnDateDismiss) },
            containerColor = AtelierBackground,
        ) {
            DayDetailContent(
                detail = detail,
                callbacks = DayDetailCallbacks(
                    onClose = { viewModel.onIntent(HistoryIntent.OnDateDismiss) },
                    onDeleteWeight = { viewModel.onIntent(HistoryIntent.OnDeleteWeight(detail.date)) },
                    onDeletePhoto = { viewModel.onIntent(HistoryIntent.OnDeletePhoto(it)) },
                    onSeePhotosClick = onSeePhotosClick,
                ),
            )
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = AtelierBackground,
        topBar = {
            AtelierAppBar(title = stringResource(R.string.history_title))
        },
        bottomBar = bottomBar,
        snackbarHost = { HSnackbarHost(snackbarHostState) },
    ) { padding ->
        HistoryPagerContent(
            state = state,
            today = today,
            callbacks = HistoryPagerCallbacks(
                onMonthChange = { viewModel.onIntent(HistoryIntent.OnMonthChange(it)) },
                onDateClick = { viewModel.onIntent(HistoryIntent.OnDateSelected(it)) },
                onViewSelect = { viewModel.onIntent(HistoryIntent.OnViewSelected(it)) },
                bitacora = BitacoraCallbacks(
                    onDayClick = { viewModel.onIntent(HistoryIntent.OnDateSelected(it)) },
                    onNoteClick = { viewModel.onIntent(HistoryIntent.OnNoteClick(it)) },
                    onWriteNoteClick = { viewModel.onIntent(HistoryIntent.OnWriteNoteClick) },
                ),
            ),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        )
    }
}

@Composable
private fun NoteEditorOverlay(editor: NoteEditorState, onIntent: (HistoryIntent) -> Unit) {
    NoteEditorSheet(editor = editor, onIntent = onIntent)
    if (editor.isConfirmingDelete) NoteDeleteDialog(onIntent = onIntent)
}

@Composable
private fun HistoryEffectsHandler(effects: Flow<HistoryEffect>, snackbarHostState: SnackbarHostState) {
    val failedMessage = stringResource(R.string.bitacora_note_action_failed)
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                HistoryEffect.NoteActionFailed -> snackbarHostState.showSnackbar(failedMessage)
            }
        }
    }
}

@Composable
private fun HistoryPagerContent(
    state: HistoryState,
    today: LocalDate,
    callbacks: HistoryPagerCallbacks,
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
            callbacks = callbacks,
            modifier = modifier,
        )
    }
}

@Composable
private fun HistoryPagedContent(
    state: HistoryState,
    today: LocalDate,
    callbacks: HistoryPagerCallbacks,
    modifier: Modifier = Modifier,
) {
    val referenceMonth = state.currentMonth
    val pagerState = rememberPagerState(
        initialPage = PAGER_INITIAL,
        pageCount = { PAGER_COUNT },
    )

    LaunchedEffect(pagerState.currentPage) {
        val offset = pagerState.currentPage - PAGER_INITIAL
        callbacks.onMonthChange(referenceMonth.plusMonths(offset))
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) {
        HistoryMonthPage(
            state = state,
            today = today,
            callbacks = callbacks,
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

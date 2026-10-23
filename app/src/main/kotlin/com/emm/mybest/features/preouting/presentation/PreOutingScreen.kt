package com.emm.mybest.features.preouting.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.ReminderTimePickerDialog
import kotlinx.coroutines.launch

@Composable
fun PreOutingScreen(
    viewModel: PreOutingViewModel,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    PreOutingEffects(viewModel, snackbarHostState, onOpenSettings)
    Scaffold(
        modifier = modifier,
        snackbarHost = { HSnackbarHost(hostState = snackbarHostState) },
        topBar = { AtelierAppBar(title = stringResource(R.string.pre_outing_title), onBack = onBack) },
    ) { paddingValues ->
        PreOutingBody(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun PreOutingEffects(
    viewModel: PreOutingViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenSettings: () -> Unit,
) {
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)
    val dayAlreadyDecidedMessage = stringResource(R.string.pre_outing_day_already_decided)
    val notWonNightDayMessage = stringResource(R.string.pre_outing_not_won_night_day)
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PreOutingEffect.OpenSettings -> currentOnOpenSettings()
                PreOutingEffect.DayAlreadyDecided -> launch { snackbarHostState.showSnackbar(dayAlreadyDecidedMessage) }
                PreOutingEffect.NotWonNightDay -> launch { snackbarHostState.showSnackbar(notWonNightDayMessage) }
            }
        }
    }
}

@Composable
private fun PreOutingBody(
    state: PreOutingState,
    onIntent: (PreOutingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.form.isOpen) { onIntent(PreOutingIntent.OnFormDismiss) }
    if (state.form.isOpen) {
        GoingOutForm(state = state, onIntent = onIntent, modifier = modifier)
    } else {
        PreOutingContent(state = state, onIntent = onIntent, modifier = modifier)
    }
    if (state.form.isTimePickerOpen) {
        ReminderTimePickerDialog(
            initialHour = state.form.pickerTime.hour,
            initialMinute = state.form.pickerTime.minute,
            onConfirm = { hour, minute -> onIntent(PreOutingIntent.OnReturnTimeSelected(hour, minute)) },
            onDismiss = { onIntent(PreOutingIntent.OnReturnTimeDismiss) },
        )
    }
}

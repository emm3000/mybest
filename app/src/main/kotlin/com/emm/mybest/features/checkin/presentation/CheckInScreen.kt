package com.emm.mybest.features.checkin.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.HSnackbarHost
import com.emm.mybest.ui.components.ReminderTimePickerDialog
import kotlinx.coroutines.launch

@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    CheckInEffects(viewModel = viewModel, snackbarHostState = snackbarHostState, onClose = onClose)
    Scaffold(
        modifier = modifier,
        snackbarHost = { HSnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        CheckInBody(state = state, onIntent = viewModel::onIntent, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
private fun CheckInEffects(
    viewModel: CheckInViewModel,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit,
) {
    val currentOnClose by rememberUpdatedState(onClose)
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            val messageRes = effect.messageRes() ?: return@collect currentOnClose()
            launch { snackbarHostState.showSnackbar(context.getString(messageRes)) }
        }
    }
}

@StringRes
private fun CheckInEffect.messageRes(): Int? = when (this) {
    CheckInEffect.Close -> null
    CheckInEffect.EveningAlreadyRecorded -> R.string.check_in_evening_already_recorded
    CheckInEffect.InvalidInput -> R.string.check_in_invalid_input
    CheckInEffect.ActionFailed -> R.string.check_in_action_failed
}

@Composable
private fun CheckInBody(
    state: CheckInState,
    onIntent: (CheckInIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.isLoaded) return
    when (state.step) {
        CheckInStep.NO_TASK -> CheckInEmptyContent(onIntent = onIntent, modifier = modifier)
        CheckInStep.QUESTION -> CheckInQuestionContent(state = state, onIntent = onIntent, modifier = modifier)
        CheckInStep.FORM -> CheckInFormContent(state = state, onIntent = onIntent, modifier = modifier)
        CheckInStep.RESULT -> CheckInResultContent(state = state, onIntent = onIntent, modifier = modifier)
    }
    if (state.form.isTimePickerOpen) {
        ReminderTimePickerDialog(
            initialTime = state.form.pickerTime,
            onConfirm = { hour, minute -> onIntent(CheckInIntent.OnReturnTimeSelected(hour, minute)) },
            onDismiss = { onIntent(CheckInIntent.OnReturnTimeDismiss) },
        )
    }
}

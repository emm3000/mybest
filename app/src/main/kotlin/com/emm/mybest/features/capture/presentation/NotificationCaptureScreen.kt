package com.emm.mybest.features.capture.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HAlertDialog
import com.emm.mybest.ui.components.HSnackbarHost
import kotlinx.coroutines.flow.Flow

@Composable
fun NotificationCaptureScreen(
    viewModel: NotificationCaptureViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LifecycleResumeEffect(viewModel) {
        viewModel.onIntent(NotificationCaptureIntent.OnResume)
        onPauseOrDispose {}
    }
    NotificationCaptureEffectsHandler(effects = viewModel.effect, snackbarHostState = snackbarHostState)
    if (state.isDeleteConfirmationVisible) {
        DeleteAllDialog(onIntent = viewModel::onIntent)
    }
    Scaffold(
        modifier = modifier,
        topBar = { AtelierAppBar(title = stringResource(R.string.capture_title), onBack = onBack) },
        snackbarHost = { HSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        NotificationCaptureContent(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun NotificationCaptureEffectsHandler(
    effects: Flow<NotificationCaptureEffect>,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val failedMessage = stringResource(R.string.capture_action_failed)
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                NotificationCaptureEffect.ActionFailed -> snackbarHostState.showSnackbar(failedMessage)
                NotificationCaptureEffect.OpenAccessSettings -> {
                    if (!context.openNotificationAccessSettings()) snackbarHostState.showSnackbar(failedMessage)
                }
            }
        }
    }
}

private fun Context.openNotificationAccessSettings(): Boolean =
    try {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

@Composable
private fun DeleteAllDialog(
    onIntent: (NotificationCaptureIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.capture_delete_title),
        description = stringResource(R.string.capture_delete_message),
        confirmText = stringResource(R.string.capture_delete_confirm),
        cancelText = stringResource(R.string.capture_cancel),
        onConfirm = { onIntent(NotificationCaptureIntent.OnDeleteAllConfirm) },
        onDismiss = { onIntent(NotificationCaptureIntent.OnDeleteAllDismiss) },
        isDangerous = true,
    )
}

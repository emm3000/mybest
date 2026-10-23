package com.emm.mybest.features.debts.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.HAlertDialog
import com.emm.mybest.ui.components.HSnackbarHost
import kotlinx.coroutines.flow.Flow

@Composable
fun DebtsScreen(
    viewModel: DebtsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    DebtsEffectsHandler(effects = viewModel.effect, snackbarHostState = snackbarHostState)
    state.form?.let { DebtFormSheet(form = it, onIntent = viewModel::onIntent) }
    state.pendingDelete?.let { DebtDeleteDialog(item = it, onIntent = viewModel::onIntent) }
    Scaffold(
        modifier = modifier,
        topBar = { AtelierAppBar(title = stringResource(R.string.debts_title), onBack = onBack) },
        snackbarHost = { HSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        DebtsContent(state = state, onIntent = viewModel::onIntent, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
private fun DebtsEffectsHandler(
    effects: Flow<DebtsEffect>,
    snackbarHostState: SnackbarHostState,
) {
    val failedMessage = stringResource(R.string.debts_action_failed)
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                DebtsEffect.ActionFailed -> snackbarHostState.showSnackbar(failedMessage)
            }
        }
    }
}

@Composable
private fun DebtDeleteDialog(
    item: DebtItemState,
    onIntent: (DebtsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.debts_delete_title),
        description = stringResource(R.string.debts_delete_message, item.name),
        confirmText = stringResource(R.string.debts_delete_confirm),
        cancelText = stringResource(R.string.debts_form_cancel),
        onConfirm = { onIntent(DebtsIntent.OnDeleteConfirm) },
        onDismiss = { onIntent(DebtsIntent.OnDeleteDismiss) },
        isDangerous = true,
    )
}

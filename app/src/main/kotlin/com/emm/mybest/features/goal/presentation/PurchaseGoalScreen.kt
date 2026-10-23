package com.emm.mybest.features.goal.presentation

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
fun PurchaseGoalScreen(
    viewModel: PurchaseGoalViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    PurchaseGoalEffectsHandler(effects = viewModel.effect, snackbarHostState = snackbarHostState)
    state.contributionForm?.let { ContributionFormSheet(form = it, onIntent = viewModel::onIntent) }
    state.goal?.takeIf { state.isClearConfirmationVisible }?.let {
        ClearGoalDialog(
            goal = it,
            onIntent = viewModel::onIntent,
        )
    }
    Scaffold(
        modifier = modifier,
        topBar = { AtelierAppBar(title = stringResource(R.string.purchase_goal_title), onBack = onBack) },
        snackbarHost = { HSnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        PurchaseGoalContent(state = state, onIntent = viewModel::onIntent, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
private fun PurchaseGoalEffectsHandler(
    effects: Flow<PurchaseGoalEffect>,
    snackbarHostState: SnackbarHostState,
) {
    val failedMessage = stringResource(R.string.purchase_goal_action_failed)
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                PurchaseGoalEffect.ActionFailed -> snackbarHostState.showSnackbar(failedMessage)
            }
        }
    }
}

@Composable
private fun ClearGoalDialog(
    goal: GoalSummaryState,
    onIntent: (PurchaseGoalIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    HAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.purchase_goal_clear_title),
        description = stringResource(R.string.purchase_goal_clear_message, goal.name),
        confirmText = stringResource(R.string.purchase_goal_clear_confirm),
        cancelText = stringResource(R.string.purchase_goal_cancel),
        onConfirm = { onIntent(PurchaseGoalIntent.OnClearConfirm) },
        onDismiss = { onIntent(PurchaseGoalIntent.OnClearDismiss) },
        isDangerous = true,
    )
}

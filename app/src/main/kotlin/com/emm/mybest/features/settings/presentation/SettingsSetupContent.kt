package com.emm.mybest.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsSetupContent(
    section: SettingsSetupSection,
    host: SettingsSetupHost,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsEffectsHandler(effects = viewModel.effect, snackbarHostState = host.snackbarHostState)
    SettingsEditors(state = state, onIntent = viewModel::onIntent)
    DismissEditorsOnLeave(section = section, onIntent = viewModel::onIntent)
    Column(modifier = modifier) {
        SettingsSetupBody(
            section = section,
            state = state,
            actions = SettingsSetupActions(
                onFieldOpen = { viewModel.onIntent(SettingsIntent.OnFieldOpen(it)) },
                onReminderIntent = viewModel::onIntent,
                onDebtsClick = host.onDebtsClick,
                onPurchaseGoalClick = host.onPurchaseGoalClick,
            ),
        )
    }
}

@Composable
private fun DismissEditorsOnLeave(
    section: SettingsSetupSection,
    onIntent: (SettingsIntent) -> Unit,
) {
    DisposableEffect(section) {
        onDispose {
            onIntent(SettingsIntent.OnFieldDismiss)
            onIntent(SettingsIntent.OnReminderDismiss)
        }
    }
}

@Composable
private fun SettingsSetupBody(
    section: SettingsSetupSection,
    state: SettingsState,
    actions: SettingsSetupActions,
) {
    if (section.fields.isNotEmpty()) {
        SettingsGoalsSection(
            params = SettingsGoalsSectionParams(
                rows = section.goalRowsIn(state.goalRows),
                onFieldOpen = actions.onFieldOpen,
            ),
        )
    }
    if (section.hasMoneyRows) {
        SettingsDebtsRow(debts = state.debts, onClick = actions.onDebtsClick)
        SettingsPurchaseGoalRow(purchaseGoal = state.purchaseGoal, onClick = actions.onPurchaseGoalClick)
    }
    if (section.hasReminders) {
        SettingsRemindersSection(setup = state.goals.setup, onIntent = actions.onReminderIntent)
    }
}

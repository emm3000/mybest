package com.emm.mybest.features.summary.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    callbacks: SummaryCallbacks,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentCallbacks by rememberUpdatedState(callbacks)
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SummaryEffect.OpenSettings -> currentCallbacks.onOpenSettings()
                SummaryEffect.OpenAddWeight -> currentCallbacks.onOpenAddWeight()
                SummaryEffect.OpenDebts -> currentCallbacks.onOpenDebts()
                SummaryEffect.OpenPurchaseGoal -> currentCallbacks.onOpenPurchaseGoal()
            }
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = { AtelierAppBar(title = stringResource(R.string.summary_title), onBack = onBack) },
    ) { paddingValues ->
        SummaryContent(state = state, onIntent = viewModel::onIntent, modifier = Modifier.padding(paddingValues))
    }
}

package com.emm.mybest.features.preouting.presentation

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
fun PreOutingScreen(
    viewModel: PreOutingViewModel,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PreOutingEffect.OpenSettings -> currentOnOpenSettings()
                PreOutingEffect.DayAlreadyDecided -> Unit
            }
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = { AtelierAppBar(title = stringResource(R.string.pre_outing_title), onBack = onBack) },
    ) { paddingValues ->
        PreOutingContent(
            state = state,
            onPromptClick = { viewModel.onIntent(PreOutingIntent.OnPromptClick(it)) },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

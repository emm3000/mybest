package com.emm.mybest.features.onboarding.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
    stepBody: @Composable (OnboardingStepHost) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.canGoBack) { viewModel.onIntent(OnboardingIntent.Back) }
    OnboardingContent(
        state = state,
        actions = OnboardingContentActions(onIntent = viewModel::onIntent, stepBody = stepBody),
        modifier = modifier,
    )
}

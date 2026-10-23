package com.emm.mybest.features.onboarding.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.mybest.R
import com.emm.mybest.ui.components.AtelierAppBar
import com.emm.mybest.ui.components.ButtonVariant
import com.emm.mybest.ui.components.HButton
import com.emm.mybest.ui.theme.AtelierTheme

private val PRIMARY_GUTTER = 28.dp
private val PRIMARY_BOTTOM = 24.dp

@Composable
fun OnboardingContent(
    state: OnboardingState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { OnboardingAppBar(state = state, onSkip = { onIntent(OnboardingIntent.Skip) }) },
        bottomBar = { OnboardingPrimaryButton(state.primaryAction) { onIntent(OnboardingIntent.Next) } },
    ) { paddingValues ->
        OnboardingStepPage(
            step = state.step,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
private fun OnboardingAppBar(
    state: OnboardingState,
    onSkip: () -> Unit,
) {
    AtelierAppBar(
        title = stringResource(R.string.onboarding_step_counter, state.position, state.total),
        actions = {
            HButton(
                text = stringResource(R.string.onboarding_skip),
                onClick = onSkip,
                variant = ButtonVariant.Ghost,
            )
        },
    )
}

@Composable
private fun OnboardingPrimaryButton(
    action: OnboardingPrimaryAction,
    onClick: () -> Unit,
) {
    HButton(
        text = stringResource(action.labelRes),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = PRIMARY_GUTTER, end = PRIMARY_GUTTER, bottom = PRIMARY_BOTTOM),
    )
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun OnboardingContentWelcomePreview() {
    AtelierTheme {
        OnboardingContent(state = OnboardingState(OnboardingGate.REQUIRED, OnboardingStep.WELCOME), onIntent = {})
    }
}

@Preview(backgroundColor = 0xFF0A0A0C, showBackground = true)
@Composable
private fun OnboardingContentLastStepPreview() {
    AtelierTheme {
        OnboardingContent(state = OnboardingState(OnboardingGate.REQUIRED, OnboardingStep.WEIGHT), onIntent = {})
    }
}

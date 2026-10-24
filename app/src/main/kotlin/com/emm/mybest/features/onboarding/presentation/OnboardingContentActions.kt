package com.emm.mybest.features.onboarding.presentation

import androidx.compose.runtime.Composable

data class OnboardingContentActions(
    val onIntent: (OnboardingIntent) -> Unit,
    val stepBody: @Composable (OnboardingStepHost) -> Unit = {},
)

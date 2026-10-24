package com.emm.mybest.features.onboarding.presentation

import androidx.compose.material3.SnackbarHostState

data class OnboardingStepHost(
    val step: OnboardingStep,
    val snackbarHostState: SnackbarHostState,
)

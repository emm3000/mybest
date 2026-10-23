package com.emm.mybest.features.onboarding.presentation

sealed class OnboardingIntent {
    data object Next : OnboardingIntent()
    data object Back : OnboardingIntent()
    data object Skip : OnboardingIntent()
}

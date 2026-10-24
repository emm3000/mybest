package com.emm.mybest.core.navigation

import com.emm.mybest.features.onboarding.presentation.OnboardingStep
import com.emm.mybest.features.settings.presentation.SettingsSetupSection

internal fun OnboardingStep.toSetupSection(): SettingsSetupSection? = when (this) {
    OnboardingStep.WELCOME -> null
    OnboardingStep.CHECKS -> SettingsSetupSection.CHECKS
    OnboardingStep.OUTINGS -> SettingsSetupSection.OUTINGS
    OnboardingStep.MONEY -> SettingsSetupSection.MONEY
    OnboardingStep.WEIGHT -> SettingsSetupSection.WEIGHT
}

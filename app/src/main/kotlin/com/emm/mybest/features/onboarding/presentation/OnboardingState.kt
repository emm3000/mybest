package com.emm.mybest.features.onboarding.presentation

data class OnboardingState(
    val gate: OnboardingGate = OnboardingGate.LOADING,
    val step: OnboardingStep = OnboardingStep.WELCOME,
) {
    val position: Int get() = step.ordinal + 1

    val total: Int get() = OnboardingStep.entries.size

    val primaryAction: OnboardingPrimaryAction
        get() = if (position == total) OnboardingPrimaryAction.FINISH else OnboardingPrimaryAction.NEXT

    val canGoBack: Boolean get() = step != OnboardingStep.WELCOME
}

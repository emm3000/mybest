package com.emm.mybest.features.onboarding.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R

@get:StringRes
internal val OnboardingStep.titleRes: Int
    get() = when (this) {
        OnboardingStep.WELCOME -> R.string.onboarding_welcome_title
        OnboardingStep.CHECKS -> R.string.onboarding_checks_title
        OnboardingStep.OUTINGS -> R.string.onboarding_outings_title
        OnboardingStep.MONEY -> R.string.onboarding_money_title
        OnboardingStep.WEIGHT -> R.string.onboarding_weight_title
    }

@get:StringRes
internal val OnboardingStep.whyRes: Int
    get() = when (this) {
        OnboardingStep.WELCOME -> R.string.onboarding_welcome_why
        OnboardingStep.CHECKS -> R.string.onboarding_checks_why
        OnboardingStep.OUTINGS -> R.string.onboarding_outings_why
        OnboardingStep.MONEY -> R.string.onboarding_money_why
        OnboardingStep.WEIGHT -> R.string.onboarding_weight_why
    }

internal val OnboardingStep.lineResIds: List<Int>
    get() = when (this) {
        OnboardingStep.WELCOME -> WELCOME_LINES
        else -> emptyList()
    }

@get:StringRes
internal val OnboardingPrimaryAction.labelRes: Int
    get() = when (this) {
        OnboardingPrimaryAction.NEXT -> R.string.onboarding_next
        OnboardingPrimaryAction.FINISH -> R.string.onboarding_finish
    }

private val WELCOME_LINES = listOf(
    R.string.onboarding_welcome_line_decide,
    R.string.onboarding_welcome_line_limits,
    R.string.onboarding_welcome_line_week,
)

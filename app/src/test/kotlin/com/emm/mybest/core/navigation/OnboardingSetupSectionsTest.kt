package com.emm.mybest.core.navigation

import com.emm.mybest.features.onboarding.presentation.OnboardingStep
import com.emm.mybest.features.settings.presentation.SettingsField
import com.emm.mybest.features.settings.presentation.SettingsSetupSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingSetupSectionsTest {

    @Test
    fun `welcome step edits nothing`() {
        assertNull(OnboardingStep.WELCOME.toSetupSection())
    }

    @Test
    fun `each setup step maps to its own section`() {
        assertEquals(SettingsSetupSection.CHECKS, OnboardingStep.CHECKS.toSetupSection())
        assertEquals(SettingsSetupSection.OUTINGS, OnboardingStep.OUTINGS.toSetupSection())
        assertEquals(SettingsSetupSection.MONEY, OnboardingStep.MONEY.toSetupSection())
        assertEquals(SettingsSetupSection.WEIGHT, OnboardingStep.WEIGHT.toSetupSection())
    }

    @Test
    fun `every settings field is edited in exactly one onboarding step`() {
        val placed = OnboardingStep.entries.mapNotNull { it.toSetupSection() }.flatMap { it.fields }

        assertEquals(SettingsField.entries.toSet(), placed.toSet())
        assertEquals(SettingsField.entries.size, placed.size)
    }
}

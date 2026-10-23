package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.testing.FakeUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsPreOutingReminderDueUseCaseTest {

    @Test
    fun `is due when notifications are turned on`() = runTest {
        val useCase = IsPreOutingReminderDueUseCase(FakeUserPreferencesRepository(isNotificationsEnabled = true))

        assertTrue(useCase())
    }

    @Test
    fun `is not due when notifications are turned off`() = runTest {
        val useCase = IsPreOutingReminderDueUseCase(FakeUserPreferencesRepository(isNotificationsEnabled = false))

        assertFalse(useCase())
    }
}

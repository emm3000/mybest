package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.reminder.WeightReminderScheduler
import com.emm.mybest.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Test

class UpdateDefaultReminderTimeUseCaseTest {

    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val scheduler = mockk<WeightReminderScheduler>()
    private val useCase = UpdateDefaultReminderTimeUseCase(userPreferencesRepository, scheduler)

    @Test
    fun `invoke with non-null time saves preference and schedules reminder`() = runTest {
        val time = LocalTime(7, 0)
        coEvery { userPreferencesRepository.setReminderTime(time) } returns Unit
        coEvery { scheduler.schedule(time) } returns Unit

        useCase(time)

        coVerify(exactly = 1) { userPreferencesRepository.setReminderTime(time) }
        coVerify(exactly = 1) { scheduler.schedule(time) }
    }

    @Test
    fun `invoke with null time clears preference and cancels reminder`() = runTest {
        coEvery { userPreferencesRepository.setReminderTime(null) } returns Unit
        coEvery { scheduler.cancel() } returns Unit

        useCase(null)

        coVerify(exactly = 1) { userPreferencesRepository.setReminderTime(null) }
        coVerify(exactly = 1) { scheduler.cancel() }
    }
}

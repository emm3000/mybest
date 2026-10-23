package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.reminder.WeightReminderScheduler
import com.emm.mybest.testing.FakeSetupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateWeighInReminderTimeUseCaseTest {

    private val scheduler = mockk<WeightReminderScheduler>()

    private fun useCase(repository: FakeSetupRepository) = UpdateWeighInReminderTimeUseCase(repository, scheduler)

    private fun stubScheduler() {
        coEvery { scheduler.schedule(any()) } returns Unit
        coEvery { scheduler.cancel() } returns Unit
    }

    @Test
    fun `setting a weigh-in time saves it in the setup and schedules the weight reminder`() = runTest {
        stubScheduler()
        val repository = FakeSetupRepository()
        val time = LocalTime(7, 0)

        useCase(repository)(time)

        assertEquals(listOf(Setup(weighInReminderTime = time)), repository.saved)
        coVerify(exactly = 1) { scheduler.schedule(time) }
    }

    @Test
    fun `clearing the weigh-in time saves it unset and cancels the weight reminder`() = runTest {
        stubScheduler()
        val repository = FakeSetupRepository(Setup(weighInReminderTime = LocalTime(7, 0)))

        useCase(repository)(null)

        assertEquals(listOf(Setup()), repository.saved)
        coVerify(exactly = 1) { scheduler.cancel() }
    }

    @Test
    fun `changing the weigh-in time keeps every other setup value`() = runTest {
        stubScheduler()
        val existing = Setup(monthlyOutingLimit = 2, weighInDay = DayOfWeek.MONDAY, debtReminderTime = LocalTime(9, 0))
        val repository = FakeSetupRepository(existing)

        useCase(repository)(LocalTime(6, 30))

        assertEquals(listOf(existing.copy(weighInReminderTime = LocalTime(6, 30))), repository.saved)
    }

    @Test
    fun `with every reminder value unset no work is scheduled`() = runTest {
        stubScheduler()
        val repository = FakeSetupRepository()

        useCase(repository)(null)

        coVerify(exactly = 0) { scheduler.schedule(any()) }
    }
}

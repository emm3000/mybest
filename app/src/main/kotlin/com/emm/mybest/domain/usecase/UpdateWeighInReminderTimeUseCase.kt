package com.emm.mybest.domain.usecase

import com.emm.mybest.domain.reminder.WeightReminderScheduler
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime

class UpdateWeighInReminderTimeUseCase(
    private val setupRepository: SetupRepository,
    private val scheduler: WeightReminderScheduler,
) {
    /**
     * Saves [time] as the weigh-in reminder time and schedules the weight reminder; null clears it and cancels.
     */
    suspend operator fun invoke(time: LocalTime?) {
        val setup = setupRepository.observe().first()
        setupRepository.save(setup.copy(weighInReminderTime = time))
        if (time != null) {
            scheduler.schedule(time)
        } else {
            scheduler.cancel()
        }
    }
}

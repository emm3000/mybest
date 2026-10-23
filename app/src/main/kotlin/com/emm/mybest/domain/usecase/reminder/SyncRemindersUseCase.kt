package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.reminder.CheckInReminderScheduler
import com.emm.mybest.domain.reminder.PreOutingReminderScheduler
import com.emm.mybest.domain.reminder.ReminderScheduler
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

/**
 * Keeps the weekend reminders in line with the setup.
 */
class SyncRemindersUseCase(
    private val setupRepository: SetupRepository,
    private val preOutingReminderScheduler: PreOutingReminderScheduler,
    private val checkInReminderScheduler: CheckInReminderScheduler,
) {

    /**
     * Never returns: schedules or cancels each reminder whenever its time changes, including the first emission at
     * app start.
     */
    suspend operator fun invoke() = coroutineScope {
        launch { sync(Setup::preOutingReminderTime, preOutingReminderScheduler) }
        launch { sync(Setup::checkInReminderTime, checkInReminderScheduler) }
    }

    private suspend fun sync(field: (Setup) -> LocalTime?, scheduler: ReminderScheduler) {
        setupRepository.observe().map(field).distinctUntilChanged().collect { time ->
            if (time == null) scheduler.cancel() else scheduler.schedule(time)
        }
    }
}

package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.domain.models.Setup
import com.emm.mybest.domain.reminder.ReminderScheduler
import com.emm.mybest.domain.reminder.WeekendReminderSchedulers
import com.emm.mybest.domain.reminder.WeightReminderScheduler
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

/**
 * Keeps the weekend and weigh-in reminders in line with the setup.
 */
class SyncRemindersUseCase(
    private val setupRepository: SetupRepository,
    private val weekendSchedulers: WeekendReminderSchedulers,
    private val weightReminderScheduler: WeightReminderScheduler,
) {

    /**
     * Never returns: schedules or cancels each reminder whenever its time changes, including the first emission at
     * app start.
     */
    suspend operator fun invoke() = coroutineScope {
        launch { sync(Setup::preOutingReminderTime, weekendSchedulers.preOuting) }
        launch { sync(Setup::checkInReminderTime, weekendSchedulers.checkIn) }
        launch { syncWeighIn() }
    }

    private suspend fun sync(field: (Setup) -> LocalTime?, scheduler: ReminderScheduler) {
        setupRepository.observe().map(field).distinctUntilChanged().collect { time ->
            if (time == null) scheduler.cancel() else scheduler.schedule(time)
        }
    }

    private suspend fun syncWeighIn() {
        setupRepository.observe().map { it.weighInDay to it.weighInReminderTime }.distinctUntilChanged()
            .collect { (day, time) ->
                if (day == null || time == null) {
                    weightReminderScheduler.cancel()
                } else {
                    weightReminderScheduler.schedule(day, time)
                }
            }
    }
}

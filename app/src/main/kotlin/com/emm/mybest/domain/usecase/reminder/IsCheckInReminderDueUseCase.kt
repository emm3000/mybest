package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

/**
 * Decides whether the check-in reminder should notify now.
 */
class IsCheckInReminderDueUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val outingRepository: OutingRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Whether notifications are on and the previous app day has an outing still pending its check-in.
     */
    suspend operator fun invoke(): Boolean {
        if (!userPreferencesRepository.notificationsEnabled.first()) return false
        val previousDay = appDayClock.today().minus(1, DateTimeUnit.DAY)
        return outingRepository.observeRange(previousDay, previousDay).first().any { it.isPendingCheckIn }
    }
}

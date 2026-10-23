package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.NightDecision
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.Tonight
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.models.isWonNightDay
import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class ObserveTonightUseCase(
    private val outingRepository: OutingRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits the current app day with the outing or won night recorded on it, recomputed when either changes or the app
     * day rolls over.
     */
    operator fun invoke(): Flow<Tonight> = appDayClock.observeToday().flatMapLatest { today ->
        combine(
            outingRepository.observeRange(today, today),
            outingRepository.observeWonNights(today, today),
        ) { outings, wonNights ->
            Tonight(today, decisionOf(outings.firstOrNull(), wonNights.firstOrNull()), today.isWonNightDay())
        }
    }

    private fun decisionOf(outing: Outing?, wonNight: WonNight?): NightDecision =
        when {
            outing != null -> NightDecision.WentOut(outing)
            wonNight != null -> NightDecision.StayedIn(wonNight)
            else -> NightDecision.Undecided
        }
}

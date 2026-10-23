package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.isWonNightDay
import com.emm.mybest.domain.models.WonNight
import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.first

class RecordWonNightUseCase(
    private val outingRepository: OutingRepository,
    private val getEstimatedOutingCost: GetEstimatedOutingCostUseCase,
    private val appDayClock: AppDayClock,
) {

    /**
     * Records a won night on the current app day with the estimated outing cost at this moment. Nothing is recorded
     * outside Friday and Saturday app days, or when the app day already has an outing or a won night. It never
     * touches debts or the purchase goal.
     */
    suspend operator fun invoke(): RecordWonNightResult {
        val today = appDayClock.today()
        if (!today.isWonNightDay()) return RecordWonNightResult.NotWonNightDay
        if (outingRepository.hasDecisionOn(today)) return RecordWonNightResult.DayAlreadyDecided
        val estimatedCost = getEstimatedOutingCost().first().cost
        outingRepository.recordWonNight(WonNight(today, estimatedCost))
        return RecordWonNightResult.Recorded(estimatedCost)
    }
}

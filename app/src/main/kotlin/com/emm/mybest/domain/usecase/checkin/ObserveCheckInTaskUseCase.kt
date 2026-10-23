package com.emm.mybest.domain.usecase.checkin

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.models.isWonNightDay
import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ObserveCheckInTaskUseCase(
    private val outingRepository: OutingRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits the oldest outing from an earlier app day that is still pending its check-in. Without one, on a Saturday
     * or Sunday app day it asks about the evening before when no outing was recorded on it and the user did not answer
     * that they stayed in. Recomputed when the records change or the app day rolls over.
     */
    operator fun invoke(): Flow<CheckInTask> = appDayClock.observeToday().flatMapLatest { today ->
        val evening = today.minus(1, DateTimeUnit.DAY)
        combine(
            outingRepository.observePending(),
            outingRepository.observeRange(evening, evening),
            outingRepository.observeNoOutingAnswers(evening, evening),
        ) { pending, eveningOutings, noOutingAnswers ->
            pending.firstOrNull { it.appDay < today }?.let(CheckInTask::PendingOuting)
                ?: askAbout(evening, isUnrecorded = eveningOutings.isEmpty() && evening !in noOutingAnswers)
        }
    }

    private fun askAbout(evening: LocalDate, isUnrecorded: Boolean): CheckInTask =
        if (isUnrecorded && evening.isWonNightDay()) CheckInTask.AskUnplanned(evening) else CheckInTask.NoTask
}

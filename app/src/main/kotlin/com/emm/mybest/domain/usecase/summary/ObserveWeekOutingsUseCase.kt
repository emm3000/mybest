package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.core.datetime.appWeekOf
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.LimitsKept
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.WeekOutings
import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class ObserveWeekOutingsUseCase(
    private val outingRepository: OutingRepository,
) {

    /**
     * Emits the outings of the app week containing [appDay]. Pending outings count but add nothing to the spent total.
     * Limits are kept by a planned outing with a check-in when none of its limits was exceeded.
     */
    operator fun invoke(appDay: LocalDate): Flow<WeekOutings> {
        val week = appWeekOf(appDay)
        return outingRepository.observeRange(week.start, week.endInclusive).map { outings ->
            WeekOutings(
                count = outings.size,
                totalSpent = outings.mapNotNull { it.checkIn?.totalSpent }.fold(Money.ZERO, Money::plus),
                pendingCheckIns = outings.count { it.isPendingCheckIn },
                limitsKept = limitsKeptOf(outings),
            )
        }
    }

    private fun limitsKeptOf(outings: List<Outing>): LimitsKept {
        val checkedIn = outings.filter { it.kind == OutingKind.PLANNED && !it.isPendingCheckIn }
        return LimitsKept(kept = checkedIn.count { !it.hasBrokenLimit }, checkedIn = checkedIn.size)
    }
}

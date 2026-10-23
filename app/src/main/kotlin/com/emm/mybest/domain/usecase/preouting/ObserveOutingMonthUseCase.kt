package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.OutingMonth
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class ObserveOutingMonthUseCase(
    private val outingRepository: OutingRepository,
    private val setupRepository: SetupRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits the outings of the current app day's month; pending outings count but add nothing to the spent total.
     */
    operator fun invoke(): Flow<OutingMonth> = appDayClock.observeToday().flatMapLatest { today ->
        val month = YearMonthValue.from(today).days()
        combine(
            outingRepository.observeRange(month.start, month.endInclusive),
            setupRepository.observe(),
        ) { outings, setup ->
            val spent = outings.mapNotNull { it.checkIn?.totalSpent }.fold(Money.ZERO, Money::plus)
            OutingMonth(spent = spent, count = outings.size, limit = setup.monthlyOutingLimit)
        }
    }
}

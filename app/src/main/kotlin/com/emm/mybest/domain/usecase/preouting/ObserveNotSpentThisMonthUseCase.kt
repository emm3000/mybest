package com.emm.mybest.domain.usecase.preouting

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.repository.OutingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class ObserveNotSpentThisMonthUseCase(
    private val outingRepository: OutingRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Emits the sum of the estimated costs of the won nights in the current app day's month; a won night without an
     * estimate counts as zero. It is never real money.
     */
    operator fun invoke(): Flow<Money> = appDayClock.observeToday().flatMapLatest { today ->
        val month = YearMonthValue.from(today).days()
        outingRepository.observeWonNights(month.start, month.endInclusive)
    }.map { wonNights ->
        wonNights.fold(Money.ZERO) { total, night -> total + (night.estimatedCost ?: Money.ZERO) }
    }
}

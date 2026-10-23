package com.emm.mybest.domain.usecase.relapse

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.OutingKind
import com.emm.mybest.domain.models.Relapse
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.repository.OutingRepository
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveRelapsesUseCase(
    private val outingRepository: OutingRepository,
    private val setupRepository: SetupRepository,
) {

    /**
     * Emits the outings that are relapses with their reasons, oldest app day first. A planned outing pending its
     * check-in has not broken a limit yet. Outings are counted per month by app day, and only those past the monthly
     * outing limit are over it; without a limit none is.
     */
    operator fun invoke(): Flow<List<Relapse>> = combine(
        outingRepository.observeAll(),
        setupRepository.observe(),
    ) { outings, setup ->
        val overLimitIds = idsOverMonthlyLimit(outings, setup.monthlyOutingLimit)
        outings.mapNotNull { outing -> relapseOf(outing, outing.id in overLimitIds) }
    }

    private fun idsOverMonthlyLimit(outings: List<Outing>, limit: Int?): Set<Long> {
        if (limit == null) return emptySet()
        return outings
            .groupBy { YearMonthValue.from(it.appDay) }
            .values
            .flatMap { month -> month.sortedBy { it.appDay }.drop(limit) }
            .mapTo(mutableSetOf()) { it.id }
    }

    private fun relapseOf(outing: Outing, isOverMonthlyLimit: Boolean): Relapse? {
        val reasons = reasonsOf(outing, isOverMonthlyLimit)
        return if (reasons.isEmpty()) null else Relapse(outing, reasons)
    }

    private fun reasonsOf(outing: Outing, isOverMonthlyLimit: Boolean): Set<RelapseReason> = buildSet {
        if (outing.kind == OutingKind.UNPLANNED) add(RelapseReason.UNPLANNED)
        if (outing.limitResults().any { it.exceededBy > 0 }) add(RelapseReason.BROKE_LIMIT)
        if (isOverMonthlyLimit) add(RelapseReason.OVER_MONTHLY_LIMIT)
    }
}

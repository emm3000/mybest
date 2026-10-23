package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.core.datetime.appWeekOf
import com.emm.mybest.domain.models.WeeklySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class ObserveWeeklySummaryUseCase(
    private val sections: WeeklySummarySections,
) {

    /**
     * Emits the summary of the Monday to Sunday app week containing [appDay]. It is composed from the use cases that
     * own each rule.
     */
    operator fun invoke(appDay: LocalDate): Flow<WeeklySummary> = combine(
        sections.outings(appDay),
        sections.recovery(appDay),
        sections.weekProgress(appDay),
        sections.weighInTrend(appWeekOf(appDay).endInclusive),
        sections.money(appDay),
        ::WeeklySummary,
    )
}

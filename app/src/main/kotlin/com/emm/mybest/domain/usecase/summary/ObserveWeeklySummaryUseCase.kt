package com.emm.mybest.domain.usecase.summary

import com.emm.mybest.domain.models.WeeklySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class ObserveWeeklySummaryUseCase(
    private val sections: WeeklySummarySections,
) {

    /**
     * Emits the summary of the Monday to Sunday app week containing [appDay]. It is composed from the use cases that
     * own each rule.
     */
    operator fun invoke(appDay: LocalDate): Flow<WeeklySummary> =
        sections.outings(appDay).map { WeeklySummary(outings = it) }
}

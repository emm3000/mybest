package com.emm.mybest.domain.usecase.weight

import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate

data class NearestWeightLookup(private val sortedEntries: List<WeightEntry>) {

    fun nearest(date: LocalDate, withinDays: Int = WINDOW_DAYS): Float? {
        if (sortedEntries.isEmpty()) return null
        val target = date.toEpochDays()
        return sortedEntries
            .minByOrNull { kotlin.math.abs(it.date.toEpochDays() - target) }
            ?.takeIf { kotlin.math.abs(it.date.toEpochDays() - target) <= withinDays }
            ?.weight
    }

    private companion object {
        const val WINDOW_DAYS = 7
    }
}

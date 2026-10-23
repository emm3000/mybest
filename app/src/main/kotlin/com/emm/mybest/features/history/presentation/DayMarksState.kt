package com.emm.mybest.features.history.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * What a calendar cell draws: [checkCount] daily checks done (0 to 4), then one mark per record in [marks].
 */
data class DayMarksState(
    val checkCount: Int,
    val marks: ImmutableList<HistoryMarkKind>,
) {
    companion object {
        val NONE = DayMarksState(checkCount = 0, marks = persistentListOf())
    }
}

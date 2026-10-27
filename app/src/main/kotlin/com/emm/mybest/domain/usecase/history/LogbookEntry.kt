package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Note
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import kotlinx.datetime.LocalTime

/**
 * One recorded event of an app day in the Logbook.
 */
sealed interface LogbookEntry {
    data class OutingEntry(val outing: Outing, val relapseReasons: Set<RelapseReason>) : LogbookEntry

    data class WonNightEntry(val wonNight: WonNight) : LogbookEntry

    data object StayedInEntry : LogbookEntry

    data class ReturnEntry(val days: Int) : LogbookEntry

    data class WeighInEntry(val weight: WeightEntry) : LogbookEntry

    data class PhotosEntry(val types: List<PhotoType>) : LogbookEntry

    data class InstallmentEntry(val debtName: String, val amount: Money) : LogbookEntry

    data class ContributionEntry(val amount: Money) : LogbookEntry

    data class NoteEntry(val note: Note, val time: LocalTime) : LogbookEntry
}

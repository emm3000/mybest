package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.money.Money
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight

/**
 * One recorded event of an app day in the Bitácora.
 */
sealed interface BitacoraEntry {
    data class OutingEntry(val outing: Outing, val relapseReasons: Set<RelapseReason>) : BitacoraEntry

    data class WonNightEntry(val wonNight: WonNight) : BitacoraEntry

    data object StayedInEntry : BitacoraEntry

    data class ReturnEntry(val days: Int) : BitacoraEntry

    data class WeighInEntry(val weight: WeightEntry) : BitacoraEntry

    data class PhotosEntry(val types: List<PhotoType>) : BitacoraEntry

    data class InstallmentEntry(val debtName: String, val amount: Money) : BitacoraEntry

    data class ContributionEntry(val amount: Money) : BitacoraEntry
}

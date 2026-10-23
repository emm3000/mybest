package com.emm.mybest.features.home.presentation

import com.emm.mybest.core.money.formatPen
import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.InstallmentShare
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.WeightEntry
import kotlinx.datetime.LocalDate

internal fun computeLastWeight(weights: List<WeightEntry>): Float? =
    weights.lastOrNull()?.weight

internal fun computePreviousWeight(weights: List<WeightEntry>): Float? =
    if (weights.size >= 2) weights[weights.size - 2].weight else null

internal fun computeLastPhotoType(photos: List<ProgressPhoto>): PhotoType? =
    photos.maxByOrNull { it.date }?.type

internal fun computeLastPhotoDaysAgo(photos: List<ProgressPhoto>, today: LocalDate): Int? {
    val lastDate = photos.maxByOrNull { it.date }?.date ?: return null
    return (today.toEpochDays() - lastDate.toEpochDays()).coerceAtLeast(0).toInt()
}

internal fun computeDailyCheckItems(inputs: HomeInputs): List<DailyCheckItem> =
    DailyCheckType.entries.map { type ->
        DailyCheckItem(
            type = type,
            answer = answerFor(inputs.recordedChecks, type),
            progress = inputs.weekProgress[type],
        )
    }

internal fun computeShortcuts(hasPendingCheckIn: Boolean): List<HomeShortcut> =
    HomeShortcut.entries.filter { hasPendingCheckIn || it != HomeShortcut.CHECK_IN }

internal fun computeKeptChecks(recordedChecks: List<DailyCheck>): Int = recordedChecks.count { it.isKept() }

internal fun computeNextInstallment(share: InstallmentShare?): HomeInstallment? = share?.let {
    HomeInstallment(it.paymentDate.formatShortMonthDay(), it.debtName, it.installment.formatPen())
}

private fun answerFor(recordedChecks: List<DailyCheck>, type: DailyCheckType): HomeCheckAnswer {
    val recorded = recordedChecks.firstOrNull { it.type == type } ?: return HomeCheckAnswer.UNANSWERED
    return if (recorded.isDone) HomeCheckAnswer.YES else HomeCheckAnswer.NO
}

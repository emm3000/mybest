package com.emm.mybest.domain.usecase.history

import com.emm.mybest.domain.models.DailyCheck
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.domain.models.Outing
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.models.RelapseReason
import com.emm.mybest.domain.models.WeightEntry
import com.emm.mybest.domain.models.WonNight
import kotlinx.datetime.LocalDate

/**
 * Everything recorded on one app day. [relapseReasons] is empty unless [outing] is a relapse, and [doneCheckCount]
 * counts the daily checks kept, from 0 to 4: gym, diet and English done, and alcohol marked as not drank.
 */
data class DaySummary(
    val date: LocalDate,
    val weight: WeightEntry? = null,
    val photos: List<ProgressPhoto> = emptyList(),
    val outing: Outing? = null,
    val relapseReasons: Set<RelapseReason> = emptySet(),
    val wonNight: WonNight? = null,
    val checks: List<DailyCheck> = emptyList(),
) {
    val hasWeight: Boolean get() = weight != null
    val hasPhoto: Boolean get() = photos.isNotEmpty()
    val hasOuting: Boolean get() = outing != null
    val isRelapse: Boolean get() = relapseReasons.isNotEmpty()
    val hasWonNight: Boolean get() = wonNight != null
    val doneCheckCount: Int get() = checks.count { it.isKept() }
    val hasChecks: Boolean get() = doneCheckCount > 0
    val hasActivity: Boolean get() = hasWeight || hasPhoto || hasOuting || hasWonNight || hasChecks
}

private fun DailyCheck.isKept(): Boolean = if (type == DailyCheckType.ALCOHOL) !isDone else isDone

package com.emm.mybest.features.home.presentation

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

package com.emm.mybest.features.history.presentation

import com.emm.mybest.R
import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.usecase.history.HistoryRecentEntry
import kotlinx.datetime.LocalDate

internal fun HistoryRecentEntry.toRowState(today: LocalDate): HistoryRecentRowState =
    HistoryRecentRowState(
        date = date,
        dateLabel = formatRecentDate(date),
        description = HistoryRecentDescription(
            weight = weight,
            photoLabelResIds = photoTypes.distinct().map { it.toLabelRes() },
            photoCount = photoTypes.size,
        ),
        isToday = date == today,
    )

private fun PhotoType.toLabelRes(): Int = when (this) {
    PhotoType.TRUNK -> R.string.photo_type_trunk
    PhotoType.FACE -> R.string.photo_type_face
}

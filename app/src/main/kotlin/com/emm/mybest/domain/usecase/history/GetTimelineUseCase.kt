package com.emm.mybest.domain.usecase.history

import com.emm.mybest.core.datetime.YearMonthValue
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

data class TimelineResult(
    val photosByDate: Map<LocalDate, List<ProgressPhoto>>,
    val photosByMonth: Map<YearMonthValue, List<ProgressPhoto>>,
)

class GetTimelineUseCase(
    private val photoRepository: PhotoRepository,
) {
    operator fun invoke(): Flow<TimelineResult> = photoRepository.getAllPhotos()
        .map { photos ->
            val sorted = photos.sortedByDescending { it.createdAt }
            TimelineResult(
                photosByDate = photos.groupBy { it.date },
                photosByMonth = sorted.groupBy { photo -> YearMonthValue.from(photo.date) },
            )
        }
}

package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.models.PhotoType
import com.emm.mybest.domain.models.ProgressPhoto
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.usecase.weight.GetNearestWeightOnDateUseCase
import com.emm.mybest.domain.usecase.weight.NearestWeightLookup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetPhotosOverviewUseCase(
    private val photoRepository: PhotoRepository,
    private val getNearestWeightOnDateUseCase: GetNearestWeightOnDateUseCase,
) {
    operator fun invoke(): Flow<PhotosOverview> = combine(
        photoRepository.getAllPhotos(),
        getNearestWeightOnDateUseCase(),
    ) { photos, weightLookup ->
        buildOverview(photos, weightLookup)
    }

    private fun buildOverview(
        photos: List<ProgressPhoto>,
        weightLookup: NearestWeightLookup,
    ): PhotosOverview {
        val byType = PhotoType.entries.associateWith { type ->
            buildTypeOverview(
                photos = photos.filter { it.type == type },
                weightLookup = weightLookup,
            )
        }
        return PhotosOverview(byType = byType)
    }

    private fun buildTypeOverview(
        photos: List<ProgressPhoto>,
        weightLookup: NearestWeightLookup,
    ): PhotoTypeOverview {
        val sorted = photos.sortedBy { it.createdAt }
        val before = sorted.firstOrNull()
        val after = if (sorted.size > 1) sorted.lastOrNull() else null
        return PhotoTypeOverview(
            before = before,
            after = after,
            timeline = sorted,
            beforeWeightKg = before?.let { weightLookup.nearest(it.date) },
            afterWeightKg = after?.let { weightLookup.nearest(it.date) },
        )
    }
}

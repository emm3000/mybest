package com.emm.mybest.domain.usecase

import com.emm.mybest.core.datetime.currentDate
import com.emm.mybest.domain.models.HomeSummary
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeSummaryUseCase(
    private val weightRepository: WeightRepository,
    private val photoRepository: PhotoRepository,
    private val getDailyHabitsUseCase: GetDailyHabitsUseCase
) {
    operator fun invoke(): Flow<HomeSummary> {
        return combine(
            weightRepository.getWeightProgress(),
            photoRepository.getAllPhotos(),
            getDailyHabitsUseCase(currentDate())
        ) { weights, photos, habits ->
            val latestEntry = weights.lastOrNull()?.weight
            val initialEntry = weights.firstOrNull()?.weight ?: 0f

            HomeSummary(
                dailyHabits = habits,
                latestWeight = latestEntry,
                totalWeightLost = if (latestEntry != null) initialEntry - latestEntry else 0f,
                totalPhotos = photos.size
            )
        }
    }
}

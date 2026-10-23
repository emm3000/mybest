package com.emm.mybest.domain.usecase.reminder

import com.emm.mybest.core.datetime.AppDayClock
import com.emm.mybest.domain.repository.PhotoRepository
import com.emm.mybest.domain.repository.SetupRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.daysUntil

private const val PHOTO_INTERVAL_DAYS = 28

/**
 * Decides whether the weigh-in reminder should also ask for a progress photo.
 */
class IsPhotoReminderDueUseCase(
    private val setupRepository: SetupRepository,
    private val photoRepository: PhotoRepository,
    private val appDayClock: AppDayClock,
) {

    /**
     * Whether the photo reminder is on and no progress photo exists or the latest one is at least 28 app days old.
     */
    suspend operator fun invoke(): Boolean {
        if (!setupRepository.observe().first().isPhotoReminderEnabled) return false
        val latest = photoRepository.getAllPhotos().first().maxOfOrNull { it.date } ?: return true
        return latest.daysUntil(appDayClock.today()) >= PHOTO_INTERVAL_DAYS
    }
}

package com.emm.mybest.domain.usecase.history

import com.emm.mybest.domain.models.ProgressPhoto

private const val MIN_COMPARE_PHOTOS = 2

data class ComparisonSelection(
    val before: ProgressPhoto?,
    val after: ProgressPhoto?,
)

class ResolveComparisonSelectionUseCase {
    operator fun invoke(
        photos: List<ProgressPhoto>,
        before: ProgressPhoto?,
        after: ProgressPhoto?,
    ): ComparisonSelection {
        val sortedPhotos = photos.sortedBy { it.createdAt }
        val validPhotoIds = sortedPhotos.mapTo(mutableSetOf()) { it.id }

        val resolvedBefore = before?.takeIf { it.id in validPhotoIds } ?: sortedPhotos.firstOrNull()
        val resolvedAfter = after
            ?.takeIf { it.id in validPhotoIds && it.id != resolvedBefore?.id }
            ?: sortedPhotos.lastOrNull { it.id != resolvedBefore?.id }

        if (sortedPhotos.size < MIN_COMPARE_PHOTOS) {
            return ComparisonSelection(
                before = sortedPhotos.firstOrNull(),
                after = null,
            )
        }

        return ComparisonSelection(
            before = resolvedBefore,
            after = resolvedAfter,
        )
    }
}

package com.emm.mybest.domain.usecase.photo

import com.emm.mybest.domain.models.ProgressPhoto

data class PhotoTypeOverview(
    val before: ProgressPhoto?,
    val after: ProgressPhoto?,
    val timeline: List<ProgressPhoto>,
    val beforeWeightKg: Float?,
    val afterWeightKg: Float?,
) {
    val count: Int get() = timeline.size
}

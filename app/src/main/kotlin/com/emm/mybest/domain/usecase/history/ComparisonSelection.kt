package com.emm.mybest.domain.usecase.history

import com.emm.mybest.domain.models.ProgressPhoto

data class ComparisonSelection(
    val before: ProgressPhoto?,
    val after: ProgressPhoto?,
)

package com.emm.mybest.features.home.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.domain.models.DailyCheckType
import com.emm.mybest.ui.components.checkLabelRes

@StringRes
internal fun weekLabelRes(type: DailyCheckType): Int = when (type) {
    DailyCheckType.ALCOHOL -> R.string.home_week_alcohol_label
    else -> checkLabelRes(type)
}

@StringRes
internal fun weekProgressFormatRes(item: DailyCheckItem): Int = when {
    !item.isLimit -> R.string.home_week_progress_format
    item.isOverLimit -> R.string.home_week_alcohol_over_format
    else -> R.string.home_week_alcohol_progress_format
}

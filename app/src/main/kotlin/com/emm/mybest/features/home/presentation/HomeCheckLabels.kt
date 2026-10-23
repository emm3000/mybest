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

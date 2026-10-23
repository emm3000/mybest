package com.emm.mybest.features.home.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.domain.models.DailyCheckType

@StringRes
internal fun checkLabelRes(type: DailyCheckType): Int = when (type) {
    DailyCheckType.GYM -> R.string.home_check_gym
    DailyCheckType.DIET -> R.string.home_check_diet
    DailyCheckType.ENGLISH -> R.string.home_check_english
    DailyCheckType.ALCOHOL -> R.string.home_check_alcohol
}

@StringRes
internal fun weekLabelRes(type: DailyCheckType): Int = when (type) {
    DailyCheckType.ALCOHOL -> R.string.home_week_alcohol_label
    else -> checkLabelRes(type)
}

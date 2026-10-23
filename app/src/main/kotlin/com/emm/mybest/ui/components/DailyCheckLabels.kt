package com.emm.mybest.ui.components

import androidx.annotation.StringRes
import com.emm.mybest.R
import com.emm.mybest.domain.models.DailyCheckType

@StringRes
fun checkLabelRes(type: DailyCheckType): Int = when (type) {
    DailyCheckType.GYM -> R.string.home_check_gym
    DailyCheckType.DIET -> R.string.home_check_diet
    DailyCheckType.ENGLISH -> R.string.home_check_english
    DailyCheckType.ALCOHOL -> R.string.home_check_alcohol
}

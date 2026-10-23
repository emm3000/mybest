package com.emm.mybest.features.settings.presentation

import com.emm.mybest.domain.models.DailyCheckType

enum class SettingsField(val checkType: DailyCheckType? = null) {
    TARGET_WEIGHT,
    MONTHLY_OUTING_LIMIT,
    TYPICAL_OUTING_COST,
    WEEKLY_GYM(DailyCheckType.GYM),
    WEEKLY_DIET(DailyCheckType.DIET),
    WEEKLY_ENGLISH(DailyCheckType.ENGLISH),
    WEEKLY_ALCOHOL(DailyCheckType.ALCOHOL),
}

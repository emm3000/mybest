package com.emm.mybest.features.settings.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R

@get:StringRes
internal val SettingsField.labelRes: Int
    get() = when (this) {
        SettingsField.TARGET_WEIGHT -> R.string.settings_goal_target_weight_label
        SettingsField.MONTHLY_OUTING_LIMIT -> R.string.settings_goal_monthly_limit_label
        SettingsField.TYPICAL_OUTING_COST -> R.string.settings_goal_typical_cost_label
        SettingsField.WEEKLY_GYM -> R.string.settings_goal_weekly_gym_label
        SettingsField.WEEKLY_DIET -> R.string.settings_goal_weekly_diet_label
        SettingsField.WEEKLY_ENGLISH -> R.string.settings_goal_weekly_english_label
        SettingsField.WEEKLY_ALCOHOL -> R.string.settings_goal_weekly_alcohol_label
    }

@get:StringRes
internal val SettingsFieldError.messageRes: Int
    get() = when (this) {
        SettingsFieldError.INVALID_NUMBER -> R.string.settings_goal_error_invalid_number
        SettingsFieldError.OUT_OF_RANGE -> R.string.settings_goal_error_out_of_range
        SettingsFieldError.NOT_POSITIVE -> R.string.settings_goal_error_not_positive
        SettingsFieldError.NEGATIVE -> R.string.settings_goal_error_negative
    }

package com.emm.mybest.features.home.presentation

import androidx.annotation.StringRes
import com.emm.mybest.R

@StringRes
internal fun shortcutLabelRes(shortcut: HomeShortcut): Int = when (shortcut) {
    HomeShortcut.PRE_OUTING -> R.string.home_shortcut_pre_outing
    HomeShortcut.CHECK_IN -> R.string.home_shortcut_check_in
    HomeShortcut.WEEKLY_SUMMARY -> R.string.home_shortcut_weekly_summary
}

package com.emm.mybest.data.reminder

import android.content.Context
import androidx.annotation.StringRes

fun interface ReminderText {
    fun resolve(context: Context): String
}

fun reminderText(@StringRes res: Int): ReminderText = ReminderText { context -> context.getString(res) }

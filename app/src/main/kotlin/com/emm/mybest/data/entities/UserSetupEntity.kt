package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single setup row. Money is stored as cents, times as minutes since midnight and the weigh-in day as its ISO
 * number (Monday = 1).
 */
@Entity(tableName = "user_setup")
data class UserSetupEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = SINGLE_ROW_ID,

    @ColumnInfo(name = "target_weight_kg")
    val targetWeightKg: Float? = null,

    @ColumnInfo(name = "monthly_outing_limit")
    val monthlyOutingLimit: Int? = null,

    @ColumnInfo(name = "typical_outing_cost_cents")
    val typicalOutingCostCents: Long? = null,

    @ColumnInfo(name = "gym_weekly_target")
    val gymWeeklyTarget: Int? = null,

    @ColumnInfo(name = "diet_weekly_target")
    val dietWeeklyTarget: Int? = null,

    @ColumnInfo(name = "english_weekly_target")
    val englishWeeklyTarget: Int? = null,

    @ColumnInfo(name = "alcohol_weekly_target")
    val alcoholWeeklyTarget: Int? = null,

    @ColumnInfo(name = "pre_outing_reminder_minutes")
    val preOutingReminderMinutes: Int? = null,

    @ColumnInfo(name = "check_in_reminder_minutes")
    val checkInReminderMinutes: Int? = null,

    @ColumnInfo(name = "weigh_in_day")
    val weighInDay: Int? = null,

    @ColumnInfo(name = "weigh_in_reminder_minutes")
    val weighInReminderMinutes: Int? = null,

    @ColumnInfo(name = "is_photo_reminder_enabled")
    val isPhotoReminderEnabled: Boolean = false,

    @ColumnInfo(name = "debt_reminder_minutes")
    val debtReminderMinutes: Int? = null,
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}

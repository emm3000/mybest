package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * One real contribution to the purchase goal on an app day, with its amount stored as cents.
 */
@Entity(tableName = "goal_contribution")
data class GoalContributionEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "app_day")
    val appDay: LocalDate,

    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,
)

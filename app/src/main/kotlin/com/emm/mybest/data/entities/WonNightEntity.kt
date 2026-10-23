package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * One won night per app day, with its estimated outing cost in cents when one was known.
 */
@Entity(tableName = "won_night")
data class WonNightEntity(

    @PrimaryKey
    @ColumnInfo(name = "app_day")
    val appDay: LocalDate,

    @ColumnInfo(name = "estimated_cost_cents")
    val estimatedCostCents: Long?,
)

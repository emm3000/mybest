package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.emm.mybest.domain.models.DailyCheckType
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "daily_check",
    primaryKeys = ["app_day", "type"],
)
data class DailyCheckEntity(

    @ColumnInfo(name = "app_day")
    val appDay: LocalDate,

    @ColumnInfo(name = "type")
    val type: DailyCheckType,

    @ColumnInfo(name = "is_done")
    val isDone: Boolean,
)

package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.emm.mybest.domain.models.OutingKind
import kotlinx.datetime.LocalDate

/**
 * One outing per app day. Money is stored as cents and times as minutes since midnight. The limit columns are all set
 * for a planned outing and all null for an unplanned one; the check-in columns stay null until the check-in is saved.
 */
@Entity(
    tableName = "outing",
    indices = [Index(value = ["app_day"], unique = true)],
)
data class OutingEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "app_day")
    val appDay: LocalDate,

    @ColumnInfo(name = "kind")
    val kind: OutingKind,

    @ColumnInfo(name = "limit_money_cents")
    val limitMoneyCents: Long? = null,

    @ColumnInfo(name = "limit_drinks")
    val limitDrinks: Int? = null,

    @ColumnInfo(name = "limit_return_minutes")
    val limitReturnMinutes: Int? = null,

    @ColumnInfo(name = "spent_cents")
    val spentCents: Long? = null,

    @ColumnInfo(name = "drinks")
    val drinks: Int? = null,

    @ColumnInfo(name = "return_minutes")
    val returnMinutes: Int? = null,

    @ColumnInfo(name = "mood")
    val mood: Int? = null,
)

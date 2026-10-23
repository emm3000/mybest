package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * An app day for which the user answered that they did not go out the night before.
 */
@Entity(tableName = "no_outing_answer")
data class NoOutingAnswerEntity(

    @PrimaryKey
    @ColumnInfo(name = "app_day")
    val appDay: LocalDate,
)

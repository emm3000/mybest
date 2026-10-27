package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * One Logbook note on an app day, with the instant it was written stored as epoch milliseconds.
 */
@Entity(tableName = "note", indices = [Index("app_day")])
data class NoteEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "app_day")
    val appDay: LocalDate,

    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,

    @ColumnInfo(name = "text")
    val text: String,
)

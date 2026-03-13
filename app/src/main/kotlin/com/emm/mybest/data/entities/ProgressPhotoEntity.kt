package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

@Entity(
    tableName = "progress_photo",
    indices = [
        Index(value = ["habit_record_id"]),
        Index(value = ["habit_id"]),
        Index(value = ["type"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = HabitRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_record_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ProgressPhotoEntity(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "habit_record_id")
    val habitRecordId: String? = null, // Making it nullable for now to support old photos

    @ColumnInfo(name = "habit_id")
    val habitId: String? = null,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "type")
    val type: PhotoType,

    @ColumnInfo(name = "photo_path")
    val photoPath: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)

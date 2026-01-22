package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "progress_photo",
    indices = [
        Index(value = ["date"]),
        Index(value = ["type"])
    ]
)
data class ProgressPhotoEntity(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "type")
    val type: PhotoType,

    @ColumnInfo(name = "photo_path")
    val photoPath: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

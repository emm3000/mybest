package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An app the user selected for notification capture.
 */
@Entity(tableName = "capture_app")
data class CaptureAppEntity(

    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,
)

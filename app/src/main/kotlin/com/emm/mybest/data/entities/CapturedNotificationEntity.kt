package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A raw notification from a selected app, with its post time stored as epoch milliseconds.
 * [notificationKey] is null for rows stored before the key was recorded.
 */
@Entity(
    tableName = "captured_notification",
    indices = [Index("notification_key")],
)
data class CapturedNotificationEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "text")
    val text: String?,

    @ColumnInfo(name = "posted_at_epoch_ms")
    val postedAtEpochMs: Long,

    @ColumnInfo(name = "notification_key")
    val notificationKey: String?,
)

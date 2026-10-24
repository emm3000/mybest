package com.emm.mybest.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File

internal fun readLiveIdentityHash(database: AppDatabase): String? =
    database.openHelper.readableDatabase
        .query(IDENTITY_HASH_QUERY, arrayOf(ROOM_MASTER_ROW_ID))
        .use { it.firstStringOrNull() }

internal fun readBackupIdentityHash(backupCopy: File): String? =
    SQLiteDatabase.openDatabase(backupCopy.path, null, BACKUP_OPEN_FLAGS).use { it.readIdentityHash() }

private fun SQLiteDatabase.readIdentityHash(): String? {
    if (!hasRoomMasterTable()) return null
    return rawQuery(IDENTITY_HASH_QUERY, arrayOf(ROOM_MASTER_ROW_ID)).use { it.firstStringOrNull() }
}

private fun SQLiteDatabase.hasRoomMasterTable(): Boolean =
    rawQuery(ROOM_MASTER_TABLE_QUERY, null).use { it.moveToFirst() }

private fun Cursor.firstStringOrNull(): String? = if (moveToFirst()) getString(0) else null

private const val ROOM_MASTER_ROW_ID = "42"
private const val IDENTITY_HASH_QUERY = "SELECT identity_hash FROM room_master_table WHERE id = ?"
private const val ROOM_MASTER_TABLE_QUERY =
    "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'room_master_table'"
private const val BACKUP_OPEN_FLAGS = SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.NO_LOCALIZED_COLLATORS

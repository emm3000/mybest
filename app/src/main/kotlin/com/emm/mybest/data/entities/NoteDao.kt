package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query(
        "SELECT * FROM note WHERE app_day BETWEEN :from AND :to ORDER BY created_at_epoch_ms DESC, id DESC",
    )
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<NoteEntity>>

    /**
     * Replaces the text of the note with [id] and returns the number of rows changed: 1, or 0 for an unknown id.
     */
    @Query("UPDATE note SET text = :text WHERE id = :id")
    suspend fun updateText(id: Long, text: String): Int

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun delete(id: Long)
}

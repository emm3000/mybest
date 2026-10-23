package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface NoOutingAnswerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(answer: NoOutingAnswerEntity)

    @Query(
        """
        SELECT app_day FROM no_outing_answer
        WHERE app_day BETWEEN :from AND :to
        ORDER BY app_day ASC
    """,
    )
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<LocalDate>>
}

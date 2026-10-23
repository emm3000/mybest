package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSetupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setup: UserSetupEntity)

    @Query("SELECT * FROM user_setup WHERE id = ${UserSetupEntity.SINGLE_ROW_ID}")
    fun observe(): Flow<UserSetupEntity?>
}

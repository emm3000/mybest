package com.emm.mybest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emm.mybest.data.entities.Converters
import com.emm.mybest.data.entities.DailyHabitDao
import com.emm.mybest.data.entities.DailyHabitEntity
import com.emm.mybest.data.entities.DailyWeightDao
import com.emm.mybest.data.entities.DailyWeightEntity
import com.emm.mybest.data.entities.ProgressPhotoDao
import com.emm.mybest.data.entities.ProgressPhotoEntity

@Database(
    entities = [
        DailyHabitEntity::class,
        DailyWeightEntity::class,
        ProgressPhotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyHabitDao(): DailyHabitDao
    abstract fun dailyWeightDao(): DailyWeightDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
}
